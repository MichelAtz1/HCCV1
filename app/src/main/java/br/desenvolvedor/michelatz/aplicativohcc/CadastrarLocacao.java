package br.desenvolvedor.michelatz.aplicativohcc;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class CadastrarLocacao extends AppCompatActivity {

    private EditText textNota, textNome;

    SQLiteDatabase db;
    String BANCO = "banco.db", TABELALOCACAO = "locacao";

    private String tipo, idLocacao, buscaNota, buscaCliente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cadastro_locacao);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        // Recebe o tipo de ação que será realizada(1 para inserção e 2 para Edição)
        tipo = bundle.getString("tipo");

        textNota = (EditText) findViewById(R.id.textNota);
        textNome = (EditText) findViewById(R.id.textNome);

        if (tipo.equals("2")) { // Verifica se é para editar
            buscaLocacao();
        }
    }

    // Método que pega a ação do botão "Salvar"
    public void salvarNovaLocacao(View v) {
        final String numeroNota = textNota.getText().toString().trim();
        final String nomeCliente = textNome.getText().toString().trim();

        // Verifica se os campos obrigatórios foram preenchidos
        if ((numeroNota.equals("") || nomeCliente.equals(""))) {
            Toast.makeText(CadastrarLocacao.this, "Todos os campos são Obrigatórios", Toast.LENGTH_SHORT).show();

        } else { // se sim, verifica o tipo de ação que será tomada
            if (tipo.equals("1")) { // ação = 1, chama o metodo de adição de locação
                adicionarLocacao(numeroNota, nomeCliente);
            } else if (tipo.equals("2")) { // ação = 2, chama o metodo de edição da locação
                editarLocacao(numeroNota, nomeCliente);
            }
        }
    }

    // método responsavel por inserir os dados no banco
    private void adicionarLocacao(final String numeroNota, final String nomeCliente) {
        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);

        // Inseri os valores no banco
        ContentValues values = new ContentValues();
        values.put("NOTA", numeroNota);
        values.put("STATUS", "0");
        values.put("DATA", "00/00/0000");
        values.put("NOME", nomeCliente);

        long ultimoId = db.insert(TABELALOCACAO, null, values);
        db.close();

        String retorno = String.valueOf(ultimoId);
        Toast.makeText(getApplicationContext(), "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();

        SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();

        // Inseri o id e numero da nota da locação nas preferencias
        editor.putString("idLocacaoKey", retorno);
        editor.putString("numeroNotaKey", numeroNota);
        editor.commit();

        Intent it;
        it = new Intent(this, GerenciarLocacoes.class);
        startActivity(it);
        finish();
    }

    // Metodo que busca os dados da locação no banco e inseri nos seus respectivos campos
    private void buscaLocacao() {
        // Recebi o id da Locação por "Preferencia"
        SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
        idLocacao = sharedpreferences.getString("idLocacaoKey", null);

        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);

        // Select que busca os dados na Tabela Locação, usando o id da locação selecionada
        Cursor linhas = db.rawQuery("SELECT * FROM " + TABELALOCACAO + " WHERE ID = '" + idLocacao + "'", null);
        if (linhas.moveToFirst()) {
            do {
                // Inseri dados nas suas respsctivas variaveis
                buscaNota = linhas.getString(1);
                buscaCliente = linhas.getString(3);
            }
            while (linhas.moveToNext());
        }
        linhas.close();
        db.close();

        // seta os dados nos seus campos
        textNota.setText(buscaNota);
        textNome.setText(buscaCliente);
    }

    // método responsavel por editar os dados no banco
    private void editarLocacao(final String numeroNota, final String nomeCliente) {
        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);

        // edita os dados da locação
        db.execSQL("UPDATE " + TABELALOCACAO + " SET NOTA = '" + numeroNota + "'," + "NOME = '" + nomeCliente + "' WHERE ID = " + "'" + idLocacao + "'");
        db.close();
        Toast.makeText(getApplicationContext(), "Edição realizada com sucesso!", Toast.LENGTH_SHORT).show();


        SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();

        // salva o numero da nota nas preferencias
        editor.putString("numeroNotaKey", numeroNota);
        editor.commit();

        Intent it;
        it = new Intent(this, GerenciarLocacoes.class);
        startActivity(it);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(CadastrarLocacao.this, GerenciarLocacoes.class);
        CadastrarLocacao.this.startActivity(intent);
        finish();
    }

}
