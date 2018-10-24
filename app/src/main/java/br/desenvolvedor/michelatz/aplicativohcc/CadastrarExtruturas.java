package br.desenvolvedor.michelatz.aplicativohcc;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

public class CadastrarExtruturas extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String vaiEditar = "0", numeroPlaca =" ", selectedIdTipoEstrutura = " ", veioEdicao="0";
    private String telefonia1, telefonia2, telefonia3, telefonia4, vidaUtil, estai, selectedIdAltura, selectedIdIluminacao;
    private String selectedIdAcesso, TipoPosteSelecionado, CapacidadeSelecionado, TipoSoloSelecionado, idConsumidor, idPoste;

    private TextView textDescricaoEstrutura;
    private RadioButton radioPrimaria, radioSecundaria;

    SQLiteDatabase db;
    String BANCO = "banco.db", TABELAESTRUTURA = "estrutura";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastra_extrutura);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Aplicativo HCC");
        toolbar.setSubtitle("Inserção de Estrutura");

        //Supote ao Toobar(Apresenta Icone voltar, na barra de cima da tela)
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        textDescricaoEstrutura = (EditText) findViewById(R.id.textDescricaoEstrutura);
        radioPrimaria = (RadioButton) findViewById(R.id.radioPrimaria);
        radioSecundaria = (RadioButton) findViewById(R.id.radioSecundaria);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        // Verifica se existe dados vindos da tela anterior
        if (getIntent().getStringExtra("USERTELA") != null){
            numeroPlaca = bundle.getString("numeroPlaca");
            telefonia1 = bundle.getString("telefonia1");
            telefonia2 = bundle.getString("telefonia2");
            telefonia3 = bundle.getString("telefonia3");
            telefonia4 = bundle.getString("telefonia4");
            estai = bundle.getString("estai");
            selectedIdAltura = bundle.getString("selectedIdAltura");
            selectedIdIluminacao = bundle.getString("selectedIdIluminacao");
            selectedIdAcesso = bundle.getString("selectedIdAcesso");
            TipoPosteSelecionado = bundle.getString("TipoPosteSelecionado");
            CapacidadeSelecionado = bundle.getString("CapacidadeSelecionado");
            TipoSoloSelecionado = bundle.getString("TipoSoloSelecionado");
            vidaUtil = bundle.getString("vidautil");

            // Verifica se a tela anterior estava sendo editada
            if (getIntent().getStringExtra("edit")!= null && getIntent().getStringExtra("edit").equals("1")){
                veioEdicao = "1";
            }

            // Verifica se esta estrutura vai ser editada
            if (getIntent().getStringExtra("USERTELA").equals("EDITAR")){// Se sim, busca dados de edição
                vaiEditar = "1";
                idConsumidor = bundle.getString("id");
                preencheDadosEdicaoExtrutura(idConsumidor);
            }
        }
    }

    // Metodo responsavel em buscar os dados da Estrutura no banco e inseri-los nos seus respctivos campos
    private void preencheDadosEdicaoExtrutura(String idConsumidor) {
        String tipo = null;
        String descricao = null;

        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);

        // Select que busca dados da estrutra
        Cursor linhas = db.rawQuery("SELECT * FROM " + TABELAESTRUTURA + " WHERE ID = " + idConsumidor + ";", null);
        if (linhas.moveToFirst()) {
            do {
                //coloca os dados nas suas respectivas variaveis
                tipo = linhas.getString(1);
                descricao = linhas.getString(2);
            }
            while (linhas.moveToNext());
            linhas.close();
        }
        db.close();

        // Verifica se o tipo é nulo
        if (tipo != null) { // se não, verifica qual tipo é(Primaria, secundaria), e seta seu respectivo radioButton
            if (tipo.equals("Primaria")) {
                radioPrimaria.setChecked(true);
                selectedIdTipoEstrutura = "Primaria";
            } else if (tipo.equals("Secundaria")) {
                radioSecundaria.setChecked(true);
                selectedIdTipoEstrutura = "Secundaria";
            }
        }
        if (descricao != null) { // Verifica se a drecricao é nula, se não é nula, seta o texto no editText
            textDescricaoEstrutura.setText(descricao);
        }
    }

    //Metodo que pega ação do botão voltar no toolbar bem em cima da tela
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setMessage("Tem certeza que deseja sair desta aba? Os dados ainda não foram salvos");
                alertDialogBuilder.setPositiveButton("Sim",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {

                                //Verifica se a pagina anterior estava sendo editada
                                if(veioEdicao.equals("1")){ // se sim, manda um putExtra(edit) avisando para continuar a edição
                                    Intent intent = new Intent(CadastrarExtruturas.this, InseriPoste.class);
                                    intent.putExtra("USERTELA","5");
                                    intent.putExtra("numeroPlaca",numeroPlaca);
                                    intent.putExtra("telefonia1",telefonia1);
                                    intent.putExtra("telefonia2",telefonia2);
                                    intent.putExtra("telefonia3",telefonia3);
                                    intent.putExtra("telefonia4",telefonia4);
                                    intent.putExtra("estai",estai);
                                    intent.putExtra("selectedIdAltura",String.valueOf(selectedIdAltura));
                                    intent.putExtra("selectedIdIluminacao",String.valueOf(selectedIdIluminacao));
                                    intent.putExtra("selectedIdAcesso", selectedIdAcesso);
                                    intent.putExtra("TipoPosteSelecionado",TipoPosteSelecionado);
                                    intent.putExtra("CapacidadeSelecionado",CapacidadeSelecionado);
                                    intent.putExtra("TipoSoloSelecionado",TipoSoloSelecionado);
                                    intent.putExtra("vidautil",vidaUtil);
                                    intent.putExtra("edit","1");
                                    CadastrarExtruturas.this.startActivity(intent);
                                    finish();
                                }else{ // se não, envia os dados que estavam sendo inseridos(Caso hajam)
                                    Intent intent = new Intent(CadastrarExtruturas.this, InseriPoste.class);
                                    intent.putExtra("USERTELA","5");
                                    intent.putExtra("numeroPlaca",numeroPlaca);
                                    intent.putExtra("telefonia1",telefonia1);
                                    intent.putExtra("telefonia2",telefonia2);
                                    intent.putExtra("telefonia3",telefonia3);
                                    intent.putExtra("telefonia4",telefonia4);
                                    intent.putExtra("estai",estai);
                                    intent.putExtra("selectedIdAltura",String.valueOf(selectedIdAltura));
                                    intent.putExtra("selectedIdIluminacao",String.valueOf(selectedIdIluminacao));
                                    intent.putExtra("selectedIdAcesso", selectedIdAcesso);
                                    intent.putExtra("TipoPosteSelecionado",TipoPosteSelecionado);
                                    intent.putExtra("CapacidadeSelecionado",CapacidadeSelecionado);
                                    intent.putExtra("TipoSoloSelecionado",TipoSoloSelecionado);
                                    intent.putExtra("vidautil",vidaUtil);

                                    CadastrarExtruturas.this.startActivity(intent);
                                    finish();
                                }
                            }
                        });
                alertDialogBuilder.setNegativeButton("Não",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {

                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                break;
            default:break;
        }
        return true;
    }

    //Metodo responsavel por pegar ação do botão nativo "Voltar" do smartfone
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("Tem certeza que deseja sair desta aba? Os dados ainda não foram salvos");
            alertDialogBuilder.setPositiveButton("Sim",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                            //Verifica se a pagina anterior estava sendo editada
                            if(veioEdicao.equals("1")){ // se sim, manda um putExtra(edit) avisando para continuar a edição
                                Intent intent = new Intent(CadastrarExtruturas.this, InseriPoste.class);
                                intent.putExtra("USERTELA","5");
                                intent.putExtra("numeroPlaca",numeroPlaca);
                                intent.putExtra("telefonia1",telefonia1);
                                intent.putExtra("telefonia2",telefonia2);
                                intent.putExtra("telefonia3",telefonia3);
                                intent.putExtra("telefonia4",telefonia4);
                                intent.putExtra("estai",estai);
                                intent.putExtra("selectedIdAltura",String.valueOf(selectedIdAltura));
                                intent.putExtra("selectedIdIluminacao",String.valueOf(selectedIdIluminacao));
                                intent.putExtra("selectedIdAcesso", selectedIdAcesso);
                                intent.putExtra("TipoPosteSelecionado",TipoPosteSelecionado);
                                intent.putExtra("CapacidadeSelecionado",CapacidadeSelecionado);
                                intent.putExtra("TipoSoloSelecionado",TipoSoloSelecionado);
                                intent.putExtra("vidautil",vidaUtil);
                                intent.putExtra("edit","1");
                                CadastrarExtruturas.this.startActivity(intent);
                                finish();
                            }else{ // se não, envia os dados que estavam sendo inseridos(Caso hajam)
                                Intent intent = new Intent(CadastrarExtruturas.this, InseriPoste.class);
                                intent.putExtra("USERTELA","5");
                                intent.putExtra("numeroPlaca",numeroPlaca);
                                intent.putExtra("telefonia1",telefonia1);
                                intent.putExtra("telefonia2",telefonia2);
                                intent.putExtra("telefonia3",telefonia3);
                                intent.putExtra("telefonia4",telefonia4);
                                intent.putExtra("estai",estai);
                                intent.putExtra("selectedIdAltura",String.valueOf(selectedIdAltura));
                                intent.putExtra("selectedIdIluminacao",String.valueOf(selectedIdIluminacao));
                                intent.putExtra("selectedIdAcesso", selectedIdAcesso);
                                intent.putExtra("TipoPosteSelecionado",TipoPosteSelecionado);
                                intent.putExtra("CapacidadeSelecionado",CapacidadeSelecionado);
                                intent.putExtra("TipoSoloSelecionado",TipoSoloSelecionado);
                                intent.putExtra("vidautil",vidaUtil);
                                CadastrarExtruturas.this.startActivity(intent);
                                finish();
                            }
                        }
                    });
            alertDialogBuilder.setNegativeButton("Não",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    // Navegação no menu
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.nav_locacoes) {
            SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.remove("idPosteKey");
            editor.commit();
            editor.clear();
            Intent it;
            it = new Intent(this, GerenciarLocacoes.class);
            startActivity(it);
            finish();
        } else if (id == R.id.nav_locacoesfinalizadas) {
            SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.remove("idPosteKey");
            editor.commit();
            editor.clear();
            Intent it;
            it = new Intent(this, ListaLocacoesFinalizadas.class);
            startActivity(it);
            finish();
        } else if (id == R.id.nav_sair) {
            SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.remove("numeroNotaKey");
            editor.remove("idPosteKey");
            editor.remove("idLocacaoKey");
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

    // Pega ação do botão "Salvar" da tela
    public void salvarEstrutura(View v){

        // Chama o metodo responsavel por salvar no banco os dados inseridos
        salvarEstruturaBanco();

        //Verifica se a pagina anterior estava sendo editada
        if(veioEdicao.equals("1")){ // se sim, manda um putExtra(edit) avisando para continuar a edição
            Intent intent = new Intent(CadastrarExtruturas.this, InseriPoste.class);
            intent.putExtra("USERTELA","5");
            intent.putExtra("numeroPlaca",numeroPlaca);
            intent.putExtra("telefonia1",telefonia1);
            intent.putExtra("telefonia2",telefonia2);
            intent.putExtra("telefonia3",telefonia3);
            intent.putExtra("telefonia4",telefonia4);
            intent.putExtra("estai",estai);
            intent.putExtra("selectedIdAltura",String.valueOf(selectedIdAltura));
            intent.putExtra("selectedIdIluminacao",String.valueOf(selectedIdIluminacao));
            intent.putExtra("selectedIdAcesso", selectedIdAcesso);
            intent.putExtra("TipoPosteSelecionado",TipoPosteSelecionado);
            intent.putExtra("CapacidadeSelecionado",CapacidadeSelecionado);
            intent.putExtra("TipoSoloSelecionado",TipoSoloSelecionado);
            intent.putExtra("vidautil",vidaUtil);
            intent.putExtra("edit","1");
            CadastrarExtruturas.this.startActivity(intent);
            finish();
        }else{ // se não, envia os dados que estavam sendo inseridos(Caso hajam)
            Intent intent = new Intent(this, InseriPoste.class);
            intent.putExtra("USERTELA","5");
            intent.putExtra("numeroPlaca",numeroPlaca);
            intent.putExtra("telefonia1",telefonia1);
            intent.putExtra("telefonia2",telefonia1);
            intent.putExtra("telefonia3",telefonia3);
            intent.putExtra("telefonia4",telefonia4);
            intent.putExtra("estai",estai);
            intent.putExtra("selectedIdAltura",String.valueOf(selectedIdAltura));
            intent.putExtra("selectedIdIluminacao",String.valueOf(selectedIdIluminacao));
            intent.putExtra("selectedIdAcesso", selectedIdAcesso);
            intent.putExtra("TipoPosteSelecionado",TipoPosteSelecionado);
            intent.putExtra("CapacidadeSelecionado",CapacidadeSelecionado);
            intent.putExtra("TipoSoloSelecionado",TipoSoloSelecionado);
            intent.putExtra("vidautil",vidaUtil);
            this.startActivity(intent);
            finish();
        }
    }

    // Método responsavel por inserir os novos dados no banco
    public void salvarEstruturaBanco(){

        SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
        idPoste = sharedpreferences.getString("idPosteKey", null);
        String descricaoEstru = textDescricaoEstrutura.getText().toString();
        descricaoEstru = descricaoEstru.toUpperCase();

        // Verifica se é uma edição
        if(vaiEditar.equals("1")){ // se sim, usa um update
            db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
            ContentValues values = new ContentValues();
            values.put("TIPO", selectedIdTipoEstrutura);
            values.put("DESCRICAO", descricaoEstru);
            db.update(TABELAESTRUTURA, values, "ID=" + idConsumidor, null);
            db.close();
        }else{ // se não, usa um insert
            if (selectedIdTipoEstrutura.equals("Primaria")) {
                db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
                ContentValues values = new ContentValues();
                values.put("TIPO", selectedIdTipoEstrutura);
                values.put("DESCRICAO", descricaoEstru);
                values.put("IDPOSTE", idPoste);
                db.insert(TABELAESTRUTURA, null, values);
                db.close();
            } else if (selectedIdTipoEstrutura.equals("Secundaria")) {
                db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
                ContentValues values = new ContentValues();
                values.put("TIPO", selectedIdTipoEstrutura);
                values.put("DESCRICAO", descricaoEstru);
                values.put("IDPOSTE", idPoste);
                db.insert(TABELAESTRUTURA, null, values);
                db.close();
            }
        }
    }

    // Pega ação do clique no RadioButton Primaria
    public void clicouPrimaria(View v){
        selectedIdTipoEstrutura = "Primaria";
    }

    // Pega ação do clique no RadioButton Secundaria
    public void clicouSecundaria(View v){
        selectedIdTipoEstrutura = "Secundaria";
    }

}
