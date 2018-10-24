package br.desenvolvedor.michelatz.aplicativohcc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class TelaPrincipal extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    SQLiteDatabase db;
    String BANCO = "banco.db";
    String TABELAFOTOSPOSTE = "fotosposte", TABELAUSUARIO = "usuario", TABELACOORDENADAS = "coordenadas", TABELALOCACAO = "locacao", TABELADOCUMENTO = "documento";
    String TABELAPOSTE = "poste", TABELACONSUMIDOR = "consumidor", TABELAEQUIPAMENTO = "equipamento", TABELAESTRUTURA = "estrutura";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        criarBanco();

        // Recebe as preferencias logo que entra na tela inicial
        SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
        String idUsuario = sharedpreferences.getString("idKey", null);

        // Verifica se existe id de usuario salvo nas preferencias
        if (idUsuario == null) { // se não existir, redireciona para  atela de Login
            Intent it = new Intent(this, Login.class);
            startActivity(it);
            finish();
        }

        // Toolbar setando titulo e subtitulo
        toolbar.setTitle("Aplicativo da HCC");
        toolbar.setSubtitle("Inicio");
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    //Metodo responsavel por pegar ação do botão nativo "Voltar" do smartfone
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            Intent it;
            it = new Intent(this, TelaPrincipal.class);
            startActivity(it);
            finish();
        }
    }

    // Ações de navegação no menu
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.nav_locacoes) {
            Intent it;
            it = new Intent(this, GerenciarLocacoes.class);
            startActivity(it);
            finish();
        } else if (id == R.id.nav_locacoesfinalizadas) {
            Intent it;
            it = new Intent(this, ListaLocacoesFinalizadas.class);
            startActivity(it);
            finish();
        } else if (id == R.id.nav_sair) { // ao sair, remover das preferencias de id, nome e e-mail
            SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.remove("idKey");
            editor.remove("nomeKey");
            editor.remove("emailKey");

            editor.commit();
            editor.clear();
            Intent it;
            it = new Intent(this, Login.class);
            startActivity(it);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Metodo que cria as tabelas do banco de dados do aplicativo
    public void criarBanco() {

        /*Metodo openOrCreateDatabase tenta criar o banco caso ele nao exista. Caso ja exista, simplesmente abre o banco */
        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);

        //CRIANDO TABELA USUARIO
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABELAUSUARIO + " (" + "ID INTEGER PRIMARY KEY, " +
                "NOME TEXT, " +
                "EMAIL TEXT, " +
                "SENHA TEXT);");

        //CRIANDO TABELA COORDENADAS
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABELACOORDENADAS + " (" + "ID INTEGER PRIMARY KEY, " +
                "LONGITUDE TEXT, " +
                "LATITUDE TEXT, " +
                "IDPOSTE INTEGER);");

        //CRIANDO TABELA LOCAÇÃO
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABELALOCACAO + " (" + "ID INTEGER PRIMARY KEY, " +
                "NOTA TEXT, " +
                "STATUS TEXT, " +
                "NOME TEXT, " +
                "DATA TEXT);");

        //CRIANDO TABELA DOCUMENTOS
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABELADOCUMENTO + " (" + "ID INTEGER PRIMARY KEY, " +
                "NOMEDOCUMENTO TEXT, " +
                "IMAGEM BLOB, " +
                "IDLOCACAO INTEGER);");

        //CRIANDO TABELA POSTE
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABELAPOSTE + " (" + "ID INTEGER PRIMARY KEY, " +
                "TIPOPOSTE TEXT, " +
                "ALTURA TEXT, " +
                "CAPACIDADE TEXT, " +
                "NUMPLACA TEXT, " +
                "ILUMICACAOPUBLICA TEXT, " +
                "TELEFONIA TEXT, " +
                "QUANTIDADE TEXT, " +
                "ACESSO TEXT, " +
                "TIPOSOLO TEXT, " +
                "FINALVIDAUTIL TEXT, " +
                "QUANT TEXT, " +
                "IDLOCACAO INTEGER);");

        //CRIANDO TABELA CONSUMIDOR
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABELACONSUMIDOR + " (" + "ID INTEGER PRIMARY KEY, " +
                "LADO TEXT, " +
                "TIPO TEXT, " +
                "CLASSE TEXT, " +
                "RAMAL TEXT, " +
                "FASE TEXT, " +
                "IDPOSTE INTEGER);");

        //CRIANDO TABELA EQUIPAMENTO
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABELAEQUIPAMENTO + " (" + "ID INTEGER PRIMARY KEY, " +
                "TIPO TEXT, " +
                "PLACA TEXT, " +
                "TENSAO TEXT, " +
                "DESCRICAO TEXT, " +
                "IDPOSTE INTEGER);");

        //CRIANDO TABELA ESTRUTURA
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABELAESTRUTURA + " (" + "ID INTEGER PRIMARY KEY, " +
                "TIPO TEXT, " +
                "DESCRICAO TEXT, " +
                "CLASSE TEXT, " +
                "NIVEL TEXT, " +
                "CRUZETA TEXT, " +
                "IDPOSTE INTEGER);");

        //CRIANDO TABELA FOTOS POSTE
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABELAFOTOSPOSTE + " (" + "ID INTEGER PRIMARY KEY, " +
                "IMAGEM BLOB, " +
                "IDPOSTE INTEGER);");

        db.close();
    }

}
