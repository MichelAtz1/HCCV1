package br.desenvolvedor.michelatz.aplicativohcc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends AppCompatActivity {

    public static final String MyPREFERENCES = "MinhasPreferencias", idUsuarioPref = "idKey", nomeUsuarioPref = "nomeKey", emailUsuarioPref = "emailKey";
    public static final String idLocacaoPref = "idLocacaoKey", idPostePref = "idPosteKey", numeroNotaPref = "numeroNotaKey";
    private EditText edtEmailUsuario, edtSenhaUsuario;

    SharedPreferences sharedpreferences;
    SQLiteDatabase db;
    String BANCO = "banco.db", TABELAUSUARIO = "usuario";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtEmailUsuario = (EditText) findViewById(R.id.edtEmailUsuario);
        edtSenhaUsuario = (EditText) findViewById(R.id.edtSenhaUsuario);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
    }

    // Metodo que pega ação do botão "Cadastre-se no Aplicativo"
    public void botaoCadastrarUsuario(View v) {
        Intent it;
        it = new Intent(this, CadastrarUsuario.class);
        startActivity(it);
        finish();
    }

    // Metodo que paga  a ação do botão "Logar"
    public void entrarAplicativo(View v) {
        final String email = edtEmailUsuario.getText().toString();
        final String senha = edtSenhaUsuario.getText().toString();

        if (email.equals("") || senha.equals("")) { // Verifica se os campos foram preenchidos
            Toast.makeText(getApplicationContext(), "Por favor, informe e-mail e senha para login!", Toast.LENGTH_SHORT).show();
        } else { // se os dados foram preenchidos, chama o metodo que ira verificar se e-mail e senha exixtem no banco de dados
            buscarSQL(email, senha);
        }
    }

    // Método que verifica que e-mail e senha existem no banco
    public void buscarSQL(String email, String senha) {
        edtEmailUsuario.setText("");
        edtSenhaUsuario.setText("");

        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);

        // Select que verifica e-mail e senha
        Cursor linhas = db.rawQuery("SELECT * FROM " + TABELAUSUARIO + " WHERE email = '" + email + "' AND senha ='" + senha + "' ", null);
        if (linhas.moveToFirst()) { //retorna false se nao ha linhas na tabela
            SharedPreferences.Editor editor = sharedpreferences.edit();
            int id = linhas.getInt(0);
            String nomeUsuario = linhas.getString(1);
            String emailUsuario = linhas.getString(2);

            // Inseri id, nome e e-mail nas preferencias
            editor.putString(idUsuarioPref, String.valueOf(id));
            editor.putString(nomeUsuarioPref, nomeUsuario);
            editor.putString(emailUsuarioPref, emailUsuario);
            editor.commit();

            Intent it;
            it = new Intent(this, TelaPrincipal.class);
            startActivity(it);
            finish();
        } else
            Toast.makeText(getApplicationContext(), "Email e/ou Senha Incorretos!", Toast.LENGTH_SHORT).show();
        db.close();
    }

}

