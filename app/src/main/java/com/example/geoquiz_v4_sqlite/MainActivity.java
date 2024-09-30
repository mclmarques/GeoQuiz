package com.example.geoquiz_v4_sqlite;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/*
  Modelo de projeto para a Atividade 1.
  Será preciso adicionar o cadastro das respostas do usuário ao Quiz, conforme
  definido no Canvas.

  GitHub: https://github.com/udofritzke/GeoQuiz
 */

public class MainActivity extends AppCompatActivity {
    //UI components
    private Button mBotaoVerdadeiro;
    private Button mBotaoFalso;
    private Button mBotaoProximo;
    private Button mBotaoMostra;
    private Button mBotaoDeleta;
    private Button mBotaoCola;
    private TextView mTextViewQuestao;
    private TextView mTextViewQuestoesArmazenadas;

    //DB initializing
    private static final String TAG = "QuizActivity";
    private static final String CHAVE_INDICE = "INDICE";
    private static final int CODIGO_REQUISICAO_COLA = 0;
    private Questao[] mBancoDeQuestoes = new Questao[]{
            new Questao(R.string.questao_suez, true),
            new Questao(R.string.questao_alemanha, false)
    };
    QuestaoDB mQuestoesDb;
    private int mIndiceAtual = 0;
    private boolean mEhColador;

    @Override
    protected void onCreate(Bundle instanciaSalva) {
        //Inicitialize data of the app
        super.onCreate(instanciaSalva);
        setContentView(R.layout.activity_main);
        //Log.d(TAG, "onCreate()");
        if (instanciaSalva != null) {
            mIndiceAtual = instanciaSalva.getInt(CHAVE_INDICE, 0);
        }
        if (mQuestoesDb == null) {
            mQuestoesDb = new QuestaoDB(getBaseContext());
        }
        int indice = 0;
        mQuestoesDb.addQuestao(mBancoDeQuestoes[indice++]);
        mQuestoesDb.addQuestao(mBancoDeQuestoes[indice++]);

        //Handle UI
        mTextViewQuestao = (TextView) findViewById(R.id.view_texto_da_questao);
        atualizaQuestao();
        mBotaoVerdadeiro = (Button) findViewById(R.id.botao_verdadeiro);
        // utilização de classe anônima interna
        mBotaoVerdadeiro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                verificaResposta(true);

            }
        });
        mBotaoFalso = (Button) findViewById(R.id.botao_falso);
        mBotaoFalso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verificaResposta(false);
            }
        });
        mBotaoProximo = (Button) findViewById(R.id.botao_proximo);
        mBotaoProximo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //The index is calculated this way so when it reaches it max value (in this case 2), it resets to 0
                mIndiceAtual = (mIndiceAtual + 1) % mBancoDeQuestoes.length;
                mEhColador = false;
                atualizaQuestao();
            }
        });

        mBotaoCola = (Button) findViewById(R.id.botao_cola);
        mBotaoCola.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // inicia ColaActivity
                // Intent intent = new Intent(MainActivity.this, ColaActivity.class);
                boolean respostaEVerdadeira = mBancoDeQuestoes[mIndiceAtual].isRespostaCorreta();
                Intent intent = ColaActivity.novoIntent(MainActivity.this, respostaEVerdadeira);
                //startActivity(intent);
                startActivityForResult(intent, CODIGO_REQUISICAO_COLA);
            }
        });

        //Cursor cur = mQuestoesDb.queryQuestao ("_id = ?", val);////(null, null);
        //String [] val = {"1"};
        mBotaoMostra = (Button) findViewById(R.id.botao_mostra_questoes);
        mBotaoMostra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cursor cursor = mQuestoesDb.queryUserResponses(null, null);
                //Build the string to print it on the UI
                StringBuilder respostasSalvas = new StringBuilder();
                if (cursor != null) {
                    try {
                        cursor.moveToFirst();
                        while (!cursor.isAfterLast()) {
                            //Get data
                            String questionUUID = cursor.getString(cursor.getColumnIndex("question_uuid"));
                            boolean userChoice = cursor.getInt(cursor.getColumnIndex("user_choice")) == 1;
                            boolean correctAnswer = cursor.getInt(cursor.getColumnIndex("correct_answer")) == 1;
                            boolean cheated = cursor.getInt(cursor.getColumnIndex("cheated")) == 1;

                            //Preparare the String
                            respostasSalvas.append("Questão UUID: ").append(questionUUID)
                                    .append("\nEscolha do usuário: ").append(userChoice ? "Verdadeiro" : "Falso")
                                    .append("\nResposta correta: ").append(correctAnswer ? "Verdadeiro" : "Falso")
                                    .append("\nColou: ").append(cheated ? "Sim" : "Não")
                                    .append("\n\n");
                            cursor.moveToNext();
                        }
                    } finally {
                        cursor.close();
                    }
                }
                mTextViewQuestoesArmazenadas = (TextView) findViewById(R.id.texto_questoes_a_apresentar);
                mTextViewQuestoesArmazenadas.setText(respostasSalvas.toString());
            }
        });
        mBotaoDeleta = (Button) findViewById(R.id.botao_deleta);
        mBotaoDeleta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                  Acesso ao SQLite
                */
                if (mQuestoesDb != null) {
                    mQuestoesDb.removeBanco();
                    if (mTextViewQuestoesArmazenadas == null) {
                        mTextViewQuestoesArmazenadas = (TextView) findViewById(R.id.texto_questoes_a_apresentar);
                    }
                    mTextViewQuestoesArmazenadas.setText("");
                    mQuestoesDb.removeAllUserResponses();
                    Toast.makeText(getApplicationContext(), "Respostas deletadas!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void atualizaQuestao() {
        int questao = mBancoDeQuestoes[mIndiceAtual].getTextoRespostaId();
        mTextViewQuestao.setText(questao);
    }

    private void verificaResposta(boolean respostaPressionada) {
        boolean respostaCorreta = mBancoDeQuestoes[mIndiceAtual].isRespostaCorreta();
        int idMensagemResposta = 0;

        if (mEhColador) {
            idMensagemResposta = R.string.toast_julgamento;
        } else {
            if (respostaPressionada == respostaCorreta) {
                idMensagemResposta = R.string.toast_correto;
            } else
                idMensagemResposta = R.string.toast_incorreto;
        }
        //Store user answer
        String questionUUID = mBancoDeQuestoes[mIndiceAtual].getId().toString();
        UserResponse respostaUsuario = new UserResponse(questionUUID, respostaPressionada, respostaCorreta, mEhColador);
        mQuestoesDb.addUserResponse(respostaUsuario);


        Toast.makeText(this, idMensagemResposta, Toast.LENGTH_SHORT).show();
    }

    //Saves current index so when the app is reloaded, it goes back to where it was
    @Override
    public void onSaveInstanceState(Bundle instanciaSalva) {
        super.onSaveInstanceState(instanciaSalva);
        Log.i(TAG, "onSaveInstanceState()");
        instanciaSalva.putInt(CHAVE_INDICE, mIndiceAtual);
    }

    //checks whether the user has cheated or not
    @Override
    protected void onActivityResult(int codigoRequisicao, int codigoResultado, Intent dados) {
        if (codigoResultado != Activity.RESULT_OK) {
            return;
        }
        if (codigoRequisicao == CODIGO_REQUISICAO_COLA) {
            if (dados == null) {
                return;
            }
            mEhColador = ColaActivity.foiMostradaResposta(dados);
        }
    }
}