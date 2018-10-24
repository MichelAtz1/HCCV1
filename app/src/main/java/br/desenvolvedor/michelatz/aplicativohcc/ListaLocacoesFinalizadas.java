package br.desenvolvedor.michelatz.aplicativohcc;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.itextpdf.text.Font;

import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import android.widget.Toast;

import br.desenvolvedor.michelatz.aplicativohcc.conexaoWeb.Config;
import br.desenvolvedor.michelatz.aplicativohcc.conexaoWeb.RequestHandler;

public class ListaLocacoesFinalizadas extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ListView.OnItemClickListener {

    private ListView listaLocacoesFinalizadas;
    SQLiteDatabase db;
    String respstaDoServidor = "0";
    String retornoArquivo = "1";
    String BANCO = "banco.db";
    String TABELALOCACAO = "locacao", TABELACOORDENADAS = "coordenadas", TABELADOCUMENTO = "documento", TABELACONSUMIDOR = "consumidor";
    String TABELAPOSTE = "poste", TABELAESTRUTURA = "estrutura", TABELAFOTOSPOSTE = "fotosposte", TABELAEQUIPAMENTO = "equipamento";
    String notaLocacaoSelecionada, idLocacaoSelect, notaEmpresa, nomeEmpresa, dataLocacao, equipeSelecionada;

    ProgressDialog progress, progress2;
    private File pdfFile;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lacacacoes_finalizadas);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Aplicativo HCC");
        toolbar.setSubtitle("Locações Finalizadas");
        setSupportActionBar(toolbar);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        listaLocacoesFinalizadas = (ListView) findViewById(R.id.listaLocacoesFinalizadas);
        listaLocacoesFinalizadas.setOnItemClickListener(this);
        preencheLista();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    // Método para pegar o "Voltar" do smartfone
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Intent it;
            it = new Intent(this, TelaPrincipal.class);
            startActivity(it);
            finish();
            super.onBackPressed();
        }
    }

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
        } else if (id == R.id.nav_sair) {
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

    // Busca no banco de dados os dados
    public void preencheLista() {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        Cursor linhas = db.rawQuery("select * from " + TABELALOCACAO + " WHERE STATUS = 1 ORDER BY DATA DESC;", null);
        if (linhas.moveToFirst()) {
            do {
                String id = linhas.getString(0);
                String nota = linhas.getString(1);
                String nome = linhas.getString(3);
                String data = linhas.getString(4);

                HashMap<String, String> servicos = new HashMap<>();
                servicos.put("ID", id);
                servicos.put("NOTA", nota);
                servicos.put("NOME", nome);
                servicos.put("DATA", data);
                list.add(servicos);
            }
            while (linhas.moveToNext());
        }
        ListAdapter adapter = new SimpleAdapter(
                ListaLocacoesFinalizadas.this, list, R.layout.list_item_relatorio,
                new String[]{"NOME", "NOTA", "DATA"},
                new int[]{R.id.txtCliente, R.id.txtCodigo, R.id.txtData});

        listaLocacoesFinalizadas.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        HashMap<String, String> map = (HashMap) parent.getItemAtPosition(position);
        final String idLocacaoSeleciona = map.get("ID").toString();
        notaLocacaoSelecionada = map.get("NOTA").toString();
        equipeSelecionada = map.get("NOME").toString();
        preencheDados(idLocacaoSeleciona);

        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("O que você deseja fazer com a Locação " + notaEmpresa + " ?");
        alertDialogBuilder.setPositiveButton("Relatórios",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (verificaConexao()) {
                            if (hasInternetAccess() != false) {


                                progress = ProgressDialog.show(ListaLocacoesFinalizadas.this, "Enviando Arquivos",
                                        "Os Arquivos estão sendo enviados ao servidor,\n Por Favor. Aguarde!", true);

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String temPasta = "nada";

                                        File root2 = new File(Environment.getExternalStorageDirectory(), "HCC/" + notaLocacaoSelecionada);

                                        if (!root2.exists()) {
                                            temPasta = "nada";
                                        } else {
                                            temPasta = "ok";
                                            String resultConsumidores = null;
                                            String resultQueda = null;
                                            String resultPostes = null;
                                            String resultPDF1 = null;
                                            String resultPDF2 = null;

                                            try {
                                                resultConsumidores = encodeFileToBase64Binary(root2 + "/" + notaLocacaoSelecionada + "_Consumidores.txt");
                                                if (resultConsumidores != null) {
                                                    //enviarTxt(notaLocacaoSelecionada + "_Consumidores.txt", resultConsumidores);
                                                    String retorno = enviarDocumentosServidor(notaLocacaoSelecionada + "_Consumidores.txt", resultConsumidores);
                                                    if(retorno.equals("2")){
                                                        retornoArquivo = "2";
                                                    }
                                                }

                                                resultQueda = encodeFileToBase64Binary(root2 + "/" + notaLocacaoSelecionada + "_Queda.txt");
                                                if (resultQueda != null) {
                                                    //enviarTxt(notaLocacaoSelecionada + "_Queda.txt", resultQueda);
                                                    String retorno = enviarDocumentosServidor(notaLocacaoSelecionada + "_Queda.txt", resultQueda);
                                                    if(retorno.equals("2")){
                                                        retornoArquivo = "2";
                                                    }
                                                }

                                                resultPostes = encodeFileToBase64Binary(root2 + "/" + notaLocacaoSelecionada + "_Postes.txt");
                                                if (resultPostes != null) {
                                                    //enviarTxt(notaLocacaoSelecionada + "_Postes.txt", resultPostes);
                                                    String retorno = enviarDocumentosServidor(notaLocacaoSelecionada + "_Postes.txt", resultPostes);
                                                    if(retorno.equals("2")){
                                                        retornoArquivo = "2";
                                                    }
                                                }

                                                resultPDF1 = encodeFileToBase64Binary(root2 + "/" + notaLocacaoSelecionada + "_Imagens.pdf");
                                                if (resultPDF1 != null) {
                                                    //enviarTxt(notaLocacaoSelecionada + "_Imagens.pdf", resultPDF1);
                                                    String retorno = enviarDocumentosServidor(notaLocacaoSelecionada + "_Imagens.pdf", resultPDF1);
                                                    if(retorno.equals("2")){
                                                        retornoArquivo = "2";
                                                    }
                                                }

                                                resultPDF2 = encodeFileToBase64Binary(root2 + "/" + notaLocacaoSelecionada + "_Equipamento.pdf");
                                                if (resultPDF2 != null) {
                                                    //enviarTxt(notaLocacaoSelecionada + "_Equipamento.pdf", resultPDF2);
                                                    String retorno = enviarDocumentosServidor(notaLocacaoSelecionada + "_Equipamento.pdf", resultPDF2);
                                                    if(retorno.equals("2")){
                                                        retornoArquivo = "2";
                                                    }
                                                }

                                                //enviarImagensPostes(idLocacaoSeleciona);
                                                //enviarImagensDocumentos(idLocacaoSeleciona);

                                                String retorno1 = enviarImagensPostes(idLocacaoSeleciona);
                                                String retorno2 = enviarImagensDocumentos(idLocacaoSeleciona);

                                                if(retorno1.equals("1") && retorno2.equals("1") && retornoArquivo.equals("1")){
                                                    respstaDoServidor = "0";
                                                }else if(retorno1.equals("2")||retorno2.equals("2") || retornoArquivo.equals("2")){
                                                    respstaDoServidor = "1";
                                                }


                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        final String finalTemPasta = temPasta;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                progress.dismiss();
                                                progress.dismiss();
                                                if (!finalTemPasta.equals("ok")) {
                                                    Toast.makeText(ListaLocacoesFinalizadas.this, "Não existe Pasta com estes dados!", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Log.d("teste",respstaDoServidor);
                                                    if(respstaDoServidor.equals("1")){
                                                        erroGeracaoArquivos();
                                                    }else if(respstaDoServidor.equals("0")){
                                                        sucessoGeracaoArquivos();
                                                    }else{
                                                        erroGeracaoArquivos();
                                                    }
                                                }
                                            }
                                        });
                                    }
                                }).start();
                            } else {
                                falhaAcessoServidor();
                            }
                        } else {
                            naoConectou();
                        }
                    }
                });

        alertDialogBuilder.setNegativeButton("Gerenciar",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        gerenciarLocacao(idLocacaoSeleciona);
                    }
                });


        android.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void erroGeracaoArquivos() {
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(Html.fromHtml("<font color='#FF0000'>Atenção - Erro no Envio de Arquivos!</font>"));
        alertDialogBuilder.setMessage(Html.fromHtml("<font color='#FF0000'>Por Favor, Reenvie os arquivos!</font>"));

        alertDialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        Runnable progressRunnable = new Runnable() {


                            @Override
                            public void run() {
                                progress.cancel();
                            }
                        };

                        Handler pdCanceller = new Handler();
                        pdCanceller.postDelayed(progressRunnable, 5000);
                    }
                });


        android.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public String enviarDocumentosServidor(final String nomeArquivo, final String arquivoBase64) {

        String retornoClasse = "1";
        HashMap<String, String> params = new HashMap<>();
        params.put(Config.KEY_ADD_NUMERO_NOTA, notaLocacaoSelecionada+"-"+equipeSelecionada);
        params.put(Config.KEY_ADD_NOME_ARQUIVO, nomeArquivo);
        params.put(Config.KEY_ADD_BASE64_ARQUIVO, arquivoBase64);

        RequestHandler rh = new RequestHandler();
        String res = rh.sendPostRequest(Config.URL_ADD_TXT, params);


        if(res.equals("1")){
            retornoClasse= "1";
        }else if(res.equals("")){
            retornoClasse= "2";
        }else{
            retornoClasse= "2";
        }

        return retornoClasse;

    }

    // Enviar Imagens dos documentos para o servidor
    public String enviarImagensDocumentos(String idLocacaoSeleciona) {
        String retornoClasse = "1";
        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        Cursor linhas = db.rawQuery("SELECT ID, NOMEDOCUMENTO, IMAGEM FROM " + TABELADOCUMENTO + " WHERE IDLOCACAO = " + idLocacaoSeleciona + ";", null);

        int contadorPassagem = 0, contadorCliente = 0, contadorProjeto = 0, contadorCroqui = 0;
        int contadorAcompanhamento = 0, contadorAmbiental = 0, contadorGarantia = 0;

        if (linhas.moveToFirst()) {
            do {
                byte[] imagemBase64 = linhas.getBlob(2);

                if (imagemBase64 != null) {
                    String imagem64 = null;
                    try {
                        imagem64 = new String(imagemBase64, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    String tipoImagem = linhas.getString(1);

                    if (tipoImagem.equals("Autorização de Passagem")) {
                        tipoImagem = "AutorizacaoPassagem" + contadorPassagem;
                        contadorPassagem++;
                    } else if (tipoImagem.equals("Cadastro de Cliente")) {
                        tipoImagem = "CadastroCliente" + contadorCliente;
                        contadorCliente++;
                    } else if (tipoImagem.equals("Carta de Apresentação do Projeto")) {
                        tipoImagem = "ApresentacaoProj" + contadorProjeto;
                        contadorProjeto++;
                    } else if (tipoImagem.equals("Croqui")) {
                        tipoImagem = "Croqui" + contadorCroqui;
                        contadorCroqui++;
                    } else if (tipoImagem.equals("Formulário de Acompanhamento")) {
                        tipoImagem = "FormAcompanhamento" + contadorAcompanhamento;
                        contadorAcompanhamento++;
                    } else if (tipoImagem.equals("Licenciamento Ambiental")) {
                        tipoImagem = "LicencAmbiental" + contadorAmbiental;
                        contadorAmbiental++;
                    } else if (tipoImagem.equals("Termo de Garantia")) {
                        tipoImagem = "TermoGarantia" + contadorGarantia;
                        contadorGarantia++;
                    }
                    //addEmployee(imagem64, tipoImagem);
                    HashMap<String, String> params = new HashMap<>();
                    params.put(Config.KEY_ADD_NUMERO_NOTA, notaLocacaoSelecionada+"-"+equipeSelecionada);
                    params.put(Config.KEY_ADD_IMAGEM, imagem64);
                    params.put(Config.KEY_ADD_TIPO_IMAGEM, tipoImagem);

                    RequestHandler rh = new RequestHandler();
                    String retornoServidor = rh.sendPostRequest(Config.URL_ADD_IMAGEM, params);

                    if(retornoServidor.equals("1")){
                        retornoClasse= "1";
                    }else if(retornoServidor.equals("")){
                        retornoClasse= "2";
                    }else{
                        retornoClasse= "2";
                    }
                }
            }
            while (linhas.moveToNext());
            linhas.close();
        }
        db.close();
        return retornoClasse;
    }

    // Enviar TXT para o servidor
    public void enviarTxt(final String nomeArquivo, final String arquivoBase64) {
        class AddEmployee extends AsyncTask<Void, Void, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
            }

            @Override
            protected String doInBackground(Void... v) {

                HashMap<String, String> params = new HashMap<>();
                params.put(Config.KEY_ADD_NUMERO_NOTA, notaLocacaoSelecionada);
                params.put(Config.KEY_ADD_NOME_ARQUIVO, nomeArquivo);
                params.put(Config.KEY_ADD_BASE64_ARQUIVO, arquivoBase64);

                RequestHandler rh = new RequestHandler();
                String res = rh.sendPostRequest(Config.URL_ADD_TXT, params);
                return res;
            }
        }
        AddEmployee ae = new AddEmployee();
        ae.execute();
    }

    // Enviar Imagens do poste para o servidor
    private String enviarImagensPostes(String idLocacaoSeleciona) {
        String retornoClasse = "1";
        int contador = 1;
        String idPoste = "";
        String id = "";

        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        Cursor linhas = db.rawQuery("SELECT * FROM " + TABELAPOSTE + " WHERE IDLOCACAO = " + idLocacaoSeleciona + ";", null);
        if (linhas.moveToFirst()) {
            do {
                idPoste = linhas.getString(0);
                if (contador < 10) {
                    id = "00" + contador;
                } else if (contador >= 10 && contador < 100) {
                    id = "0" + contador;
                } else if (contador >= 100) {
                    id = "" + String.valueOf(contador);
                }

                Cursor linhas2 = db.rawQuery("SELECT * FROM " + TABELAEQUIPAMENTO + " WHERE IDPOSTE= " + idPoste + ";", null);
                if (linhas2.moveToFirst()) {
                    do {
                        Cursor linhas3 = db.rawQuery("SELECT * FROM " + TABELAFOTOSPOSTE + " WHERE IDPOSTE= " + idPoste + ";", null);

                        if (linhas3.moveToFirst() && linhas3.getCount() > 0) {
                            int contador2 = 1;
                            do {
                                byte[] imagemBase64 = linhas3.getBlob(1);
                                if (imagemBase64 != null) {
                                    String imagemLoca = null;
                                    try {
                                        imagemLoca = new String(imagemBase64, "UTF-8");
                                    } catch (UnsupportedEncodingException e) {
                                        e.printStackTrace();
                                    }
                                    String nomeLoco = "Poste " + id + "_" + contador2;
                                    //addEmployee(imagemLoca, nomeLoco);

                                    HashMap<String, String> params = new HashMap<>();
                                    params.put(Config.KEY_ADD_NUMERO_NOTA, notaLocacaoSelecionada+"-"+equipeSelecionada);
                                    params.put(Config.KEY_ADD_IMAGEM, imagemLoca);
                                    params.put(Config.KEY_ADD_TIPO_IMAGEM, nomeLoco);

                                    RequestHandler rh = new RequestHandler();
                                    String retornoServidor = rh.sendPostRequest(Config.URL_ADD_IMAGEM, params);

                                    if(retornoServidor.equals("1")){
                                        retornoClasse= "1";
                                    }else if(retornoServidor.equals("")){
                                        retornoClasse= "2";
                                    }else{
                                        retornoClasse= "2";
                                    }

                                    contador2++;
                                }
                            }
                            while (linhas3.moveToNext());
                            linhas3.close();
                        }
                    }
                    while (linhas2.moveToNext());
                    linhas2.close();
                }
                contador++;
            }
            while (linhas.moveToNext());
            linhas.close();
        }
        db.close();
        return retornoClasse;
    }

    // Abre um Alert para seleção de um tipo de edição
    public void gerenciarLocacao(final String idLocal) {

        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("O que você deseja Gerenciar na Locação " + notaEmpresa + " ?");
        alertDialogBuilder.setPositiveButton("Excluir",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        confirmarDeleteImagens(idLocal);
                    }
                });

        alertDialogBuilder.setNegativeButton("Editar",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedpreferences.edit();

                        editor.putString("idLocacaoKey", idLocal);
                        editor.putString("numeroNotaKey", notaLocacaoSelecionada);
                        editor.commit();

                        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
                        ContentValues values = new ContentValues();
                        values.put("STATUS", "0");
                        db.update(TABELALOCACAO, values, "ID=" + idLocal, null);
                        db.close();

                        Intent intent = new Intent(ListaLocacoesFinalizadas.this, GerenciarLocacoes.class);
                        ListaLocacoesFinalizadas.this.startActivity(intent);
                        finish();
                    }
                });

        android.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    // Alert para confirmação de exclusão de uma locação
    private void confirmarDeleteImagens(final String idNotaLocac) {
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Tem certeza que deseja deletar TODOS os dados armazenados desta Locação?");

        alertDialogBuilder.setPositiveButton("Sim",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        deletarDadosLocacao(idNotaLocac);
                    }
                });

        alertDialogBuilder.setNegativeButton("Não",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                });

        android.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    // Exclui pastas do celular, utilizando o id da Nota como condição
    private void deletarDadosLocacao(String idNotaLocac) {
        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);

        db.execSQL("DELETE FROM " + TABELAFOTOSPOSTE + " WHERE IDPOSTE IN (SELECT ID FROM " + TABELAPOSTE + " WHERE IDLOCACAO = " + idNotaLocac + ")");
        db.execSQL("DELETE FROM " + TABELAESTRUTURA + " WHERE IDPOSTE IN (SELECT ID FROM " + TABELAPOSTE + " WHERE IDLOCACAO = " + idNotaLocac + ")");
        db.execSQL("DELETE FROM " + TABELAEQUIPAMENTO + " WHERE IDPOSTE IN (SELECT ID FROM " + TABELAPOSTE + " WHERE IDLOCACAO = " + idNotaLocac + ")");
        db.execSQL("DELETE FROM " + TABELACONSUMIDOR + " WHERE IDPOSTE IN (SELECT ID FROM " + TABELAPOSTE + " WHERE IDLOCACAO = " + idNotaLocac + ")");
        db.execSQL("DELETE FROM " + TABELACOORDENADAS + " WHERE IDPOSTE IN (SELECT ID FROM " + TABELAPOSTE + " WHERE IDLOCACAO = " + idNotaLocac + ")");

        db.execSQL("DELETE FROM " + TABELADOCUMENTO + " WHERE IDLOCACAO = " + idNotaLocac + "");
        db.execSQL("DELETE FROM " + TABELAPOSTE + " WHERE IDLOCACAO = " + idNotaLocac + "");
        db.execSQL("DELETE FROM " + TABELALOCACAO + " WHERE ID = " + idNotaLocac + "");

        db.close();

        excluiFotos(idNotaLocac);
        //preencheLista();
    }

    // Exclui pastas do celular
    public void excluiFotos(String idNotaLocac) {
        File root = new File(Environment.getExternalStorageDirectory(), "HCC/" + notaLocacaoSelecionada);
        if (!root.exists()) {

        } else {
            File[] files = root.listFiles();
            for (File fInDir : files) {
                fInDir.delete();
                root.delete();
            }
            excluidoComSucesso();
            preencheLista();
        }
    }

    // Método que busca no banco os dados da locação no banco e seta seus valores nos respectivos locais
    private void preencheDados(String idLocacaoSeleciona) {
        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        Cursor linhas = db.rawQuery("SELECT * FROM " + TABELALOCACAO + " WHERE ID = " + idLocacaoSeleciona + ";", null);

        if (linhas.moveToFirst()) {
            do {
                nomeEmpresa = (linhas.getString(3));
                notaEmpresa = (linhas.getString(1));
                dataLocacao = (linhas.getString(4));
            }
            while (linhas.moveToNext());
            linhas.close();
        }
        db.close();
    }

    public void createPdfWrapper() throws FileNotFoundException, DocumentException {
        int hasWriteStoragePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS)) {
                    showMessageOKCancel("Você precisa permitir o acesso ao Armazenamento",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                REQUEST_CODE_ASK_PERMISSIONS);
                                    }
                                }
                            });
                    return;
                }

                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_ASK_PERMISSIONS);
            }
            return;
        } else {
            try {
                createPdf();
                createPdfImagens();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        createPdfWrapper();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (DocumentException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(this, "Permissão Negada", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    // Cria PDF com Imagens dos dados do poste
    private void createPdf() throws IOException, DocumentException {

        int contador = 1;
        String idPoste = "";
        String conferiTexto = "";
        String textoDadosEquipamento = "";
        String id = "";

        File docsFolder = new File(Environment.getExternalStorageDirectory() + "/HCC");
        if (!docsFolder.exists()) {
            docsFolder.mkdir();
        }

        File docsFolder2 = new File(Environment.getExternalStorageDirectory() + "/HCC/" + notaLocacaoSelecionada);
        if (!docsFolder2.exists()) {
            docsFolder2.mkdir();
        }
        pdfFile = new File(docsFolder2.getAbsolutePath(), notaLocacaoSelecionada + "_Equipamento.pdf");
        OutputStream output = new FileOutputStream(pdfFile);
        Document document = new Document(PageSize.A4, 0, 0, 0, 0);
        PdfWriter.getInstance(document, output);

        document.addTitle("Equipamentos");

        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        Cursor linhas = db.rawQuery("SELECT * FROM " + TABELAPOSTE + " WHERE IDLOCACAO = " + idLocacaoSelect + ";", null);
        if (linhas.moveToFirst()) {
            document.open();
            do {
                idPoste = linhas.getString(0);
                if (contador < 10) {
                    id = "00" + contador;
                } else if (contador >= 10 && contador < 100) {
                    id = "0" + contador;
                } else if (contador >= 100) {
                    id = "" + String.valueOf(contador);
                }

                Cursor linhas2 = db.rawQuery("SELECT * FROM " + TABELAEQUIPAMENTO + " WHERE IDPOSTE= " + idPoste + ";", null);
                if (linhas2.moveToFirst()) {
                    do {
                        if (linhas2.getString(1).equals("") && linhas2.getString(2).equals("")) {//linhas2.getString(3).equals("") && linhas2.getString(4).equals("")
                            textoDadosEquipamento = "Não foram inseridos dados do Equipamento!";
                            conferiTexto = "1";
                        } else {
                            textoDadosEquipamento = "   Tipo de Equipamento: " + linhas2.getString(1) + "\n   Numero da Placa: " + linhas2.getString(2) + "\n";
                            Log.d("teste", linhas2.getString(1));
                            conferiTexto = "0";
                        }

                        Cursor linhas3 = db.rawQuery("SELECT * FROM " + TABELAFOTOSPOSTE + " WHERE IDPOSTE= " + idPoste + ";", null);

                        if (linhas3.moveToFirst() && linhas3.getCount() > 0) {
                            int contador2 = 1;
                            do {
                                byte[] imagemBase64 = linhas3.getBlob(1);
                                if (imagemBase64 != null) {
                                    String imagemLoca = new String(imagemBase64, "UTF-8");
                                    String nomeLoco = "Poste " + id + "_" + contador2;

                                    addEmployeePoste(imagemLoca, nomeLoco);
                                    contador2++;

                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                    Bitmap bitmap = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.moldecima);
                                    Bitmap tamanhoReduzidoParaCima = Bitmap.createScaledBitmap(bitmap, 589, 80, true);
                                    tamanhoReduzidoParaCima.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                    Image myImg = Image.getInstance(stream.toByteArray());
                                    myImg.setAlignment(Image.MIDDLE);
                                    document.add(myImg);

                                    ByteArrayOutputStream stream3 = new ByteArrayOutputStream();
                                    Bitmap imagemBitmap = decodeBase64(imagemBase64);
                                    Bitmap novoTamanho = Bitmap.createScaledBitmap(imagemBitmap, (int) (imagemBitmap.getWidth() * 0.35), (int) (imagemBitmap.getHeight() * 0.35), true);//tava 0.68
                                    novoTamanho.compress(Bitmap.CompressFormat.JPEG, 100, stream3);
                                    Image myImg3 = Image.getInstance(stream3.toByteArray());


                                    document.add(new Paragraph(" Poste: " + id));
                                    Paragraph paragrafo1 = new Paragraph(textoDadosEquipamento);
                                    document.add(paragrafo1);

                                    myImg3.setAlignment(Image.MIDDLE);
                                    myImg3.setAbsolutePosition(132f, 190f);
                                    document.add(myImg3);

                                    ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
                                    Bitmap bitmap2 = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.moldebaixo);
                                    Bitmap tamanhoReduzidoParaBaixo = Bitmap.createScaledBitmap(bitmap2, 589, 210, true);
                                    tamanhoReduzidoParaBaixo.compress(Bitmap.CompressFormat.JPEG, 100, stream2);
                                    Image myImg2 = Image.getInstance(stream2.toByteArray());
                                    myImg2.setAbsolutePosition(3, 3);
                                    document.add(myImg2);
                                    document.newPage();

/*
                                    HashMap<String,String> params = new HashMap<>();
                                    params.put(Config.KEY_ADD_NUMERO_NOTA,notaLocacaoSelecionada);
                                    params.put(Config.KEY_ADD_IMAGEM, String.valueOf(imagemBase64));

                                    RequestHandler rh = new RequestHandler();
                                    String res = rh.sendPostRequest(Config.URL_ADD_IMAGEM, params);

                                    Log.d("teste-resposta",res);
                                    */

                                }
                                document.newPage();
                            }
                            while (linhas3.moveToNext());
                            linhas3.close();
                        } else {

                            if (conferiTexto.equals("0")) {
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                Bitmap bitmap = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.moldecima);
                                Bitmap tamanhoReduzidoParaCima = Bitmap.createScaledBitmap(bitmap, 589, 80, true);
                                tamanhoReduzidoParaCima.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                Image myImg = Image.getInstance(stream.toByteArray());
                                myImg.setAlignment(Image.MIDDLE);
                                document.add(myImg);

                                document.add(new Paragraph(" Poste: " + id));
                                Paragraph paragrafo1 = new Paragraph(textoDadosEquipamento);
                                document.add(paragrafo1);

                                Paragraph paragrafo2 = new Paragraph(" Não foi inserida imagem deste Equipamento");
                                paragrafo2.setAlignment(Element.ALIGN_CENTER);

                                ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
                                Bitmap bitmap2 = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.moldebaixo);
                                Bitmap tamanhoReduzidoParaBaixo = Bitmap.createScaledBitmap(bitmap2, 589, 210, true);
                                tamanhoReduzidoParaBaixo.compress(Bitmap.CompressFormat.JPEG, 100, stream2);
                                Image myImg2 = Image.getInstance(stream2.toByteArray());
                                myImg2.setAbsolutePosition(3, 3);
                                document.add(myImg2);

                                document.newPage();
                            }
                        }
                    }
                    while (linhas2.moveToNext());
                    linhas2.close();
                }
                contador++;
            }
            while (linhas.moveToNext());
            linhas.close();
            document.add(new Chunk(""));
            document.close();
        }
        db.close();
        String result = encodeFileToBase64Binary(docsFolder2 + "/" + notaLocacaoSelecionada + "_Equipamento.pdf");
        addEmployeeTxt(notaLocacaoSelecionada + "_Equipamento.pdf", result);
    }

    // Cria PDF com Imagens dos documentos
    public void createPdfImagens() throws IOException, DocumentException {

        File docsFolder = new File(Environment.getExternalStorageDirectory() + "/HCC");
        if (!docsFolder.exists()) {
            docsFolder.mkdir();
        }

        File docsFolder2 = new File(Environment.getExternalStorageDirectory() + "/HCC/" + notaLocacaoSelecionada);
        if (!docsFolder2.exists()) {
            docsFolder2.mkdir();
        }

        pdfFile = new File(docsFolder2.getAbsolutePath(), notaLocacaoSelecionada + "_Imagens.pdf");
        OutputStream output = new FileOutputStream(pdfFile);
        Document document = new Document(PageSize.A4, 0, 0, 0, 0);
        PdfWriter.getInstance(document, output);

        document.addTitle("Imagens");

        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        Cursor linhas = db.rawQuery("SELECT ID, NOMEDOCUMENTO, IMAGEM FROM " + TABELADOCUMENTO + " WHERE IDLOCACAO = " + idLocacaoSelect + ";", null);

        int contadorPassagem = 0;
        int contadorCliente = 0;
        int contadorProjeto = 0;
        int contadorCroqui = 0;
        int contadorAcompanhamento = 0;
        int contadorAmbiental = 0;
        int contadorGarantia = 0;

        if (linhas.moveToFirst()) {
            document.open();
            do {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                Bitmap bitmap = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.moldecima);
                Bitmap tamanhoReduzidoParaCima = Bitmap.createScaledBitmap(bitmap, 589, 80, true);
                tamanhoReduzidoParaCima.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                Image myImg = Image.getInstance(stream.toByteArray());
                myImg.setAlignment(Image.MIDDLE);

                document.add(myImg);

                Font titleFont = new Font(Font.FontFamily.COURIER, 20, Font.BOLD);
                Paragraph paragrafo1 = new Paragraph("RELATÓRIO FOTOGRÁFICO\n");
                paragrafo1.setFont(titleFont);
                paragrafo1.setAlignment(Element.ALIGN_CENTER);
                document.add(paragrafo1);

                String nomeDocumento = linhas.getString(1);
                Paragraph paragrafo2 = new Paragraph("  " + nomeDocumento + "\n");
                document.add(paragrafo2);
                Paragraph paragrafo3 = new Paragraph("\n");

                byte[] imagemBase64 = linhas.getBlob(2);

                if (imagemBase64 != null) {
                    String imagem64 = new String(imagemBase64, "UTF-8");
                    String tipoImagem = linhas.getString(1);

                    if (tipoImagem.equals("Autorização de Passagem")) {
                        tipoImagem = "AutorizacaoPassagem" + contadorPassagem;
                        contadorPassagem++;
                    } else if (tipoImagem.equals("Cadastro de Cliente")) {
                        tipoImagem = "CadastroCliente" + contadorCliente;
                        contadorCliente++;
                    } else if (tipoImagem.equals("Carta de Apresentação do Projeto")) {
                        tipoImagem = "ApresentacaoProj" + contadorProjeto;
                        contadorProjeto++;
                    } else if (tipoImagem.equals("Croqui")) {
                        tipoImagem = "Croqui" + contadorCroqui;
                        contadorCroqui++;
                    } else if (tipoImagem.equals("Formulário de Acompanhamento")) {
                        tipoImagem = "FormAcompanhamento" + contadorAcompanhamento;
                        contadorAcompanhamento++;
                    } else if (tipoImagem.equals("Licenciamento Ambiental")) {
                        tipoImagem = "LicencAmbiental" + contadorAmbiental;
                        contadorAmbiental++;
                    } else if (tipoImagem.equals("Termo de Garantia")) {
                        tipoImagem = "TermoGarantia" + contadorGarantia;
                        contadorGarantia++;
                    }

                    addEmployee(imagem64, tipoImagem);

                    ByteArrayOutputStream stream3 = new ByteArrayOutputStream();
                    Bitmap imagemBitmap = decodeBase64(imagemBase64);

                    Bitmap novoTamanho = Bitmap.createScaledBitmap(imagemBitmap, (int) (imagemBitmap.getWidth() * 0.50), (int) (imagemBitmap.getHeight() * 0.50), true);// tava 0.71

                    novoTamanho.compress(Bitmap.CompressFormat.JPEG, 100, stream3);
                    Image myImg3 = Image.getInstance(stream3.toByteArray());
                    myImg3.setAlignment(Image.MIDDLE);

                    myImg3.setAbsolutePosition(3f, 3f);
                    document.add(myImg3);
                    document.add(paragrafo3);
                }
                document.newPage();
            }
            while (linhas.moveToNext());
            linhas.close();
            document.close();
        } else {
            document.open();

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Bitmap bitmap = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.moldecima);
            Bitmap tamanhoReduzidoParaCima = Bitmap.createScaledBitmap(bitmap, 589, 80, true);
            tamanhoReduzidoParaCima.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            Image myImg = Image.getInstance(stream.toByteArray());
            myImg.setAlignment(Image.MIDDLE);
            document.add(myImg);

            Font titleFont = new Font(Font.FontFamily.COURIER, 20, Font.BOLD);
            Paragraph paragrafo1 = new Paragraph("Não foi inserida nenhuma imagem de documentos neste relatório!\n");
            paragrafo1.setFont(titleFont);
            paragrafo1.setAlignment(Element.ALIGN_CENTER);
            document.add(paragrafo1);


            ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
            Bitmap bitmap2 = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.moldebaixo);
            Bitmap tamanhoReduzidoParaBaixo = Bitmap.createScaledBitmap(bitmap2, 589, 210, true);
            tamanhoReduzidoParaBaixo.compress(Bitmap.CompressFormat.JPEG, 100, stream2);
            Image myImg2 = Image.getInstance(stream2.toByteArray());
            myImg2.setAbsolutePosition(3, 3);
            document.add(myImg2);


            document.close();
        }
        db.close();

        String result = encodeFileToBase64Binary(docsFolder + "/" + notaLocacaoSelecionada + "_Imagens.pdf");
        addEmployeeTxt(notaLocacaoSelecionada + "_Imagens.pdf", result);
    }

    // Envia imagens para o servidor
    private void addEmployee(final String imagem64, final String tipoImagem) {

        class AddEmployee extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
            }

            @Override
            protected String doInBackground(Void... v) {
                HashMap<String, String> params = new HashMap<>();
                params.put(Config.KEY_ADD_NUMERO_NOTA, notaLocacaoSelecionada);
                params.put(Config.KEY_ADD_IMAGEM, imagem64);
                params.put(Config.KEY_ADD_TIPO_IMAGEM, tipoImagem);

                RequestHandler rh = new RequestHandler();
                String res = rh.sendPostRequest(Config.URL_ADD_IMAGEM, params);
                return res;
            }
        }

        AddEmployee ae = new AddEmployee();
        ae.execute();
    }

    // Envia imagens para o servidor
    private void addEmployeePoste(final String imagemPoste, final String nomePoste) {

        class AddEmployee extends AsyncTask<Void, Void, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
            }

            @Override
            protected String doInBackground(Void... v) {
                HashMap<String, String> params = new HashMap<>();
                params.put(Config.KEY_ADD_NUMERO_NOTA, notaLocacaoSelecionada);
                params.put(Config.KEY_ADD_IMAGEM, imagemPoste);
                params.put(Config.KEY_ADD_TIPO_IMAGEM, nomePoste);

                RequestHandler rh = new RequestHandler();
                String res = rh.sendPostRequest(Config.URL_ADD_IMAGEM, params);
                return res;
            }
        }

        AddEmployee ae = new AddEmployee();
        ae.execute();
    }

    // Envia arquivo para o servidor
    private void addEmployeeTxt(final String nomeArquivo, final String arquivoBase64) {

        class AddEmployee extends AsyncTask<Void, Void, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
            }

            @Override
            protected String doInBackground(Void... v) {
                HashMap<String, String> params = new HashMap<>();
                params.put(Config.KEY_ADD_NUMERO_NOTA, notaLocacaoSelecionada);
                params.put(Config.KEY_ADD_NOME_ARQUIVO, nomeArquivo);
                params.put(Config.KEY_ADD_BASE64_ARQUIVO, arquivoBase64);

                RequestHandler rh = new RequestHandler();
                String res = rh.sendPostRequestTxt(Config.URL_ADD_TXT, params);
                //AQUI rh.sendPostRequestTxt
                return res;
            }
        }
        AddEmployee ae = new AddEmployee();
        ae.execute();
    }

    // Codifica arquivo em byte[]
    public byte[] loadFile(String fileName) {
        File file = new File(fileName);

        byte[] bytes = new byte[(int) file.length()];
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(bytes);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bytes;
    }

    // Codifica Arquivo em base64
    public String encodeFileToBase64Binary(String fileName) throws IOException {
        byte[] bytes = loadFile(fileName);
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    // Envia os relatórios para email ou Watts
    public String enviaEmail() {

        progress2 = ProgressDialog.show(ListaLocacoesFinalizadas.this, "Selecioanndo relatórios",
                "Os Arquivos estão sendo selecionados para envio!,\n Por Favor. Aguarde!", true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Intent itEmail = new Intent(Intent.ACTION_SEND_MULTIPLE);
                itEmail.setType("application/");

                SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
                String nomeUsuario = sharedpreferences.getString("nomeKey", null);
                String emailUsuario = sharedpreferences.getString("emailKey", null);

                ArrayList<Uri> arquivosUris = new ArrayList<Uri>();

                Uri arquivo1 = Uri.parse("file:/mnt/sdcard/HCC/" + notaLocacaoSelecionada + "/" + notaLocacaoSelecionada + "_Postes.txt");
                Uri arquivo2 = Uri.parse("file:/mnt/sdcard/HCC/" + notaLocacaoSelecionada + "/" + notaLocacaoSelecionada + "_Consumidores.txt");
                Uri arquivo3 = Uri.parse("file:/mnt/sdcard/HCC/" + notaLocacaoSelecionada + "/" + notaLocacaoSelecionada + "_Equipamento.pdf");
                Uri arquivo4 = Uri.parse("file:/mnt/sdcard/HCC/" + notaLocacaoSelecionada + "/" + notaLocacaoSelecionada + "_Imagens.pdf");
                Uri arquivo5 = Uri.parse("file:/mnt/sdcard/HCC/" + notaLocacaoSelecionada + "/" + notaLocacaoSelecionada + "_Queda.txt");

                arquivosUris.add(arquivo1);
                arquivosUris.add(arquivo2);
                arquivosUris.add(arquivo3);
                arquivosUris.add(arquivo4);
                arquivosUris.add(arquivo5);

                itEmail.putParcelableArrayListExtra(Intent.EXTRA_STREAM, arquivosUris);
                itEmail.putExtra(Intent.EXTRA_SUBJECT, "Relatório HCC - " + nomeEmpresa + " - " + notaEmpresa);
                itEmail.putExtra(Intent.EXTRA_TEXT, "Relatório realizado em " + dataLocacao + " pelo colaborador " + nomeUsuario + ", de e-mail: " + emailUsuario + ". \n\nReferente ao cliente " + nomeEmpresa + ", e o numero da nota é: " + notaEmpresa);
                itEmail.putExtra(Intent.EXTRA_EMAIL, "michelatz1@gmail.com");
                startActivity(Intent.createChooser(itEmail, "Escolha a App para envio do e-mail..."));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress2.dismiss();

                    }
                });
            }
        }).start();
        return "ok";
    }

    // Codifica byte[] em Imagem
    public static Bitmap decodeBase64(byte[] input) {
        byte[] decodedBytes = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    // Verifica Conexão com a Internet
    public boolean verificaConexao() {
        boolean conectado;
        ConnectivityManager conectivtyManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conectivtyManager.getActiveNetworkInfo() != null
                && conectivtyManager.getActiveNetworkInfo().isAvailable()
                && conectivtyManager.getActiveNetworkInfo().isConnected()) {
            conectado = true;
        } else {
            conectado = false;
        }
        return conectado;
    }

    // Alert com resultado de falta de Conecção
    public void naoConectou() {
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Sem acesso a Internet");
        alertDialogBuilder.setMessage("Para enviar estes relatórios, seu celular precisa estar conectado a internet.\n\n Por Favor!\n Conecte-se a internet e tente novamente!");

        alertDialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                });
        android.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    // Verifica Conexão com a Internet
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) ListaLocacoesFinalizadas.this.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    // Verifica Tranferencia de dados
    private boolean hasInternetAccess() {
        if (isNetworkAvailable()) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://clients3.google.com/generate_204").openConnection());
                urlc.setRequestProperty("User-Agent", "Android");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                return (urlc.getResponseCode() == 204 &&
                        urlc.getContentLength() == 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // Cria um Alert, com resposta a falta de conectividade do smartfone
    public void falhaAcessoServidor() {
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Falha no Envio de Dados");
        alertDialogBuilder.setMessage("O envio de dados ao servidor falhou!\n\n Por Favor!\n Verifique seu pacotes de dados ou seu modem de distribuição Wi-Fi e tente novamente!");

        alertDialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                });


        android.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    // Cria um Alert, com a conformação da criação de arquivos
    public void sucessoGeracaoArquivos() {
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(Html.fromHtml("<font color='#000000'>Os arquivos Foram enviados!</font>"));
        alertDialogBuilder.setMessage(Html.fromHtml("<font color='#000000'>Por Favor - Envie o E-mail de aviso!</font>"));

        alertDialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        /*
                        final ProgressDialog progress = new ProgressDialog(ListaLocacoesFinalizadas.this);
                        progress.setTitle("Enviando e-mail");
                        progress.setMessage("Os documentos estão sendo selecionados pa. Por Favor! Aguarde!");
                        progress.show();
                        */
                        enviaEmail();

                        Runnable progressRunnable = new Runnable() {


                            @Override
                            public void run() {
                                progress.cancel();
                            }
                        };

                        Handler pdCanceller = new Handler();
                        pdCanceller.postDelayed(progressRunnable, 5000);
                    }
                });


        android.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    // Cria um Alert, com a confirmação da exclusão
    public void excluidoComSucesso() {
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Exclusão de Dados!");
        alertDialogBuilder.setMessage("Todos os dados foram Excluídos com Sucesso!");

        alertDialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent intent = new Intent(ListaLocacoesFinalizadas.this, ListaLocacoesFinalizadas.class);
                        ListaLocacoesFinalizadas.this.startActivity(intent);
                        finish();
                    }
                });


        android.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

}