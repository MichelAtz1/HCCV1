package br.desenvolvedor.michelatz.aplicativohcc;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class CadastrarUsuario extends AppCompatActivity {

    private EditText edtNome, edtEmail, edtSenha;
    SQLiteDatabase db;
    String BANCO = "banco.db";
    String TABELAUSUARIO = "usuario";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_usuario);

        edtNome = (EditText) findViewById(R.id.edtNome);
        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtSenha = (EditText) findViewById(R.id.edtSenha);
    }

    // Pega a ação do botão "Salvar"
    public void salvar(View v) {
        final String nome = edtNome.getText().toString().trim();
        final String email = edtEmail.getText().toString().trim();
        final String senha = edtSenha.getText().toString().trim();

        // Verifica se todos os campos foram preenchidos
        if ((nome.equals("") || email.equals("") || senha.equals(""))) {
            Toast.makeText(CadastrarUsuario.this, "Todos os campos são Obrigatórios", Toast.LENGTH_SHORT).show();
        } else {
            boolean emailValido = validaEmail(email); // Chama o metodo que valida o email digitado
            if (emailValido == true) { // se o e-mail é valido, chama o metodo para adicionar o novo usuário
                adicionarUsuario(nome, email, senha);
            } else { // se o e-mail não for valido, lança um toast pedindo para inserir um e-mail valido
                Toast.makeText(CadastrarUsuario.this, "Por Favor! Insira um E-mail valido!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Método que inseri 1 novo usuario no banco de dados
    private void adicionarUsuario(final String nome, final String email, final String senha) {
        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        db.execSQL("INSERT INTO " + TABELAUSUARIO + "(NOME, EMAIL, SENHA) VALUES ('" + nome + "','" + email + "','" + senha + "')");
        db.close();
        Toast.makeText(getApplicationContext(), "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();

        Intent it;
        it = new Intent(this, Login.class);
        startActivity(it);
        finish();
    }

    // Metodo responsável por verificar se o e-mail é válido
    private static boolean validaEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    //Metodo responsável por pegar ação do botão nativo "Voltar" do smartfone
    @Override
    public void onBackPressed() {
        Intent it;
        it = new Intent(this, Login.class);
        startActivity(it);
        finish();
        super.onBackPressed();
    }

}
