package com.example.geoquiz_v4_sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class QuestaoDB {

    private Context mContext;
    private static Context mStaticContext;
    private SQLiteDatabase mDatabase;

    public QuestaoDB(Context contexto){
        mContext = contexto.getApplicationContext();
        mStaticContext = mContext;
        mDatabase = new QuestoesDBHelper(mContext).getWritableDatabase();
    }
    private static ContentValues getValoresConteudo(Questao q){
        ContentValues valores = new ContentValues();

        // pares chave-valor: nomes das colunas - valores
        valores.put(QuestoesDbSchema.QuestoesTbl.Cols.UUID, q.getId().toString());
        valores.put(QuestoesDbSchema.QuestoesTbl.Cols.TEXTO_QUESTAO,
                mStaticContext.getString(q.getTextoRespostaId())); // recupera valor do recurso string pelo id
        valores.put(QuestoesDbSchema.QuestoesTbl.Cols.QUESTAO_CORRETA, q.isRespostaCorreta());
        return valores;
    }
    public void addQuestao(Questao q){
        ContentValues valores = getValoresConteudo(q);
        mDatabase.insert(QuestoesDbSchema.QuestoesTbl.NOME, null, valores);
    }
    public void updateQuestao(Questao q){
        String uuidString = q.getId().toString();
        ContentValues valores = getValoresConteudo(q);
       // mDatabase.update(QuestoesDbSchema.QuestoesTbl.NOME, valores, QuestoesDbSchema.QuestoesTbl.Cols.UUID +" = ?",
        //        new String[] {uuidString});
    }
    public Cursor queryQuestao(String clausulaWhere, String[] argsWhere){
        Cursor cursor = mDatabase.query(QuestoesDbSchema.QuestoesTbl.NOME,
                null,  // todas as colunas
                    clausulaWhere,
                    argsWhere,
                null, // sem group by
                null, // sem having
                null  // sem order by
                );
                return cursor;
    }
    void removeBanco(){
        int delete;
        delete = mDatabase.delete(
                QuestoesDbSchema.QuestoesTbl.NOME,
                null, null);
    }

    //Code to handle the answers stored in the DB
    public void addUserResponse(UserResponse response) {
        ContentValues valores = getUserResponseContentValues(response);
        mDatabase.insert("user_responses", null, valores);
    }

    private static ContentValues getUserResponseContentValues(UserResponse response) {
        ContentValues valores = new ContentValues();
        valores.put("question_uuid", response.getQuestionUUID());
        valores.put("user_choice", response.isUserChoice() ? 1 : 0);
        valores.put("correct_answer", response.isCorrectAnswer() ? 1 : 0);
        valores.put("cheated", response.isCheated() ? 1 : 0);
        return valores;
    }

    public Cursor queryUserResponses(String clausulaWhere, String[] argsWhere) {
        return mDatabase.query(
                "user_responses",
                null,  // Todas as colunas
                clausulaWhere,
                argsWhere,
                null, // Sem group by
                null, // Sem having
                null  // Sem order by
        );
    }

    public void removeAllUserResponses() {
        mDatabase.delete("user_responses", null, null);
    }
}
