package br.desenvolvedor.michelatz.aplicativohcc;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

import br.desenvolvedor.michelatz.aplicativohcc.Adapter.AdapterListViewGeral;
import br.desenvolvedor.michelatz.aplicativohcc.Adapter.AdapterListViewPontos;
import br.desenvolvedor.michelatz.aplicativohcc.Adapter.AdapterListViewPostes;
import br.desenvolvedor.michelatz.aplicativohcc.ClassesExtras.Helper;
import br.desenvolvedor.michelatz.aplicativohcc.Modelo.DadosGerais;

public class GerenciarLocacoes extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Spinner spnLocacoesAbertas;
    ArrayList<String> locacoesAbertas = new ArrayList<String>();
    EditText edtNumeroNota, edtCliente;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 111;

    ProgressDialog progress;
    private File pdfFile;
    SQLiteDatabase db;

    String BANCO = "banco.db";
    String TABELALOCACAO = "locacao", TABELACOORDENADAS = "coordenadas", TABELADOCUMENTO = "documento", TABELACONSUMIDOR = "consumidor";
    String TABELAPOSTE = "poste", TABELAESTRUTURA = "estrutura", TABELAFOTOSPOSTE = "fotosposte", TABELAEQUIPAMENTO = "equipamento";
    String textoEstruturaPrimaria, textoEstruturaSecundaria, texto, previa, notaLocacao, idLocacao;
    String idNota = "", buscaNota = "", buscaCliente = "";

    private ListView listViewPostes, listViewDoc, listViewPontos;
    private AdapterListViewPostes adapterListViewPostes;
    private AdapterListViewPontos adapterListViewPontos;
    private AdapterListViewGeral adapterListViewDocumentos;
    private ArrayList<DadosGerais> itens;
    private ArrayList<DadosGerais> itensDoc;
    private ArrayList<DadosGerais> itensPontos;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locacoes);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Aplicativo HCC");
        toolbar.setSubtitle("Locações");
        setSupportActionBar(toolbar);

        SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
        notaLocacao = sharedpreferences.getString("numeroNotaKey", null);
        idLocacao = sharedpreferences.getString("idLocacaoKey", null);

        spnLocacoesAbertas = (Spinner) findViewById(R.id.spnLocacoesAbertas);
        edtNumeroNota = (EditText) findViewById(R.id.edtNumeroNota);
        edtCliente = (EditText) findViewById(R.id.edtCliente);
        listViewPostes = (ListView) findViewById(R.id.listViewPostes);
        listViewPontos = (ListView) findViewById(R.id.listViewPontos);
        listViewDoc = (ListView) findViewById(R.id.listViewDocumentos);

        if (idLocacao != null) {
            buscaDados();
            inflaListaPostes();
            inflaListaPontos();
            inflaListaDocumentosComIdLocacao();
        } else if (notaLocacao != null) {
            buscaDados();
            inflaListaPostes();
            inflaListaPontos();
            inflaListaDocumentosComIdLocacao();
        }
        buscarSQLPreencheSpinner();

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, locacoesAbertas);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnLocacoesAbertas.setAdapter(adapter);

        spnLocacoesAbertas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                StringTokenizer st = new StringTokenizer(parent.getItemAtPosition(position).toString());
                previa = st.nextToken(" ");
                if (previa.equals("Selecione")) {

                } else {
                    SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedpreferences.edit();

                    buscaDadosClickSpn();
                    editor.putString("numeroNotaKey", previa);
                    //editor.putString("idLocacaoKey", previa);
                    editor.commit();

                    Intent it;
                    it = new Intent(GerenciarLocacoes.this, GerenciarLocacoes.class);
                    startActivity(it);
                    finish();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Another interface callback
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(GerenciarLocacoes.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
            return;
        }
    }

    public void buscarSQLPreencheSpinner() {
        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        Cursor linhas = db.rawQuery("SELECT * FROM " + TABELALOCACAO + " WHERE STATUS = 0", null);

        locacoesAbertas.add("Selecione uma nota");
        if (linhas.moveToFirst()) {
            do {
                String buscaNumeroNota = linhas.getString(1);
                String buscaNomeCliente = linhas.getString(3);
                locacoesAbertas.add(buscaNumeroNota + " - " + buscaNomeCliente);
            }
            while (linhas.moveToNext());
        }
        linhas.close();
        db.close();
    }

    public void buscaDados() {
        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        Cursor linhas = db.rawQuery("SELECT * FROM " + TABELALOCACAO + " WHERE ID = '" + idLocacao + "'", null);

        if (linhas.moveToFirst()) {
            do {
                buscaNota = linhas.getString(1);
                buscaCliente = linhas.getString(3);
            }
            while (linhas.moveToNext());
        }
        linhas.close();
        db.close();

        edtNumeroNota.setText(buscaNota);
        edtCliente.setText(buscaCliente);
    }

    public void buscaDadosClickSpn() {
        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        Cursor linhas = db.rawQuery("SELECT * FROM " + TABELALOCACAO + " WHERE NOTA = '" + previa + "'", null);

        if (linhas.moveToFirst()) {
            do {
                idNota = linhas.getString(0);
                buscaNota = linhas.getString(1);
                buscaCliente = linhas.getString(3);
            }
            while (linhas.moveToNext());
        }
        linhas.close();
        db.close();

        SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("idLocacaoKey", idNota);
        editor.commit();
    }

    private void inflaListaPostes() {
        itens = new ArrayList<DadosGerais>();

        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        Cursor linhas = db.rawQuery("SELECT * FROM " + TABELAPOSTE + " WHERE IDLOCACAO = " + idLocacao + ";", null);
        int contador = 1;

        if (linhas.moveToFirst()) {
            do {
                if(linhas.getString(11)!=null){
                    Log.d("teste", "Dentro inflaPostes: "+linhas.getString(11));
                }else{
                    Log.d("teste", "Fora inflaPostes: "+linhas.getString(11));
                }
                if (linhas.getString(11).equals("")) {
                    String idPoste = linhas.getString(0);
                    String texto = "Poste " + contador;

                    DadosGerais item1 = new DadosGerais(idPoste, texto);
                    itens.add(item1);
                }
                contador++;
            }
            while (linhas.moveToNext());
        }
        adapterListViewPostes = new AdapterListViewPostes(this, itens);
        listViewPostes.setAdapter(adapterListViewPostes);
        listViewPostes.setCacheColorHint(Color.TRANSPARENT);

        linhas.close();
        Helper.getListViewSize(listViewPostes);
        db.close();
    }

    private void inflaListaPontos() {
        itensPontos = new ArrayList<DadosGerais>();

        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        Cursor linhas = db.rawQuery("SELECT * FROM " + TABELAPOSTE + " WHERE IDLOCACAO = " + idLocacao + ";", null);
        int contador = 1;

        if (linhas.moveToFirst()) {
            do {
                if(linhas.getString(11)!=null){
                    Log.d("teste", "Dentro inflaPontos: "+linhas.getString(11));
                }else{
                    Log.d("teste", "Fora inflaPontos: "+linhas.getString(11));
                }
                if (!linhas.getString(11).equals("")) {
                    String idPoste = linhas.getString(0);
                    String texto = "Poste " + contador + " - " + linhas.getString(11);

                    DadosGerais item1 = new DadosGerais(idPoste, texto);
                    itensPontos.add(item1);
                }
                contador++;
            }
            while (linhas.moveToNext());
        }
        adapterListViewPontos = new AdapterListViewPontos(this, itensPontos);
        listViewPontos.setAdapter(adapterListViewPontos);
        listViewPontos.setCacheColorHint(Color.TRANSPARENT);

        linhas.close();
        Helper.getListViewSize(listViewPontos);
        db.close();
    }

    public void adicionarPonto(final View v) {
        final Dialog dialog = new Dialog(GerenciarLocacoes.this);
        dialog.setContentView(R.layout.radiocustomizado);
        dialog.setTitle("Descrição do ponto");
        dialog.setCancelable(true);

        Button dialogButton = (Button) dialog.findViewById(R.id.alertOK);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String descricaoPonto = "";
                EditText textDescricaoEstrutura = (EditText) dialog.findViewById(R.id.textDescricaoEstrutura);

                descricaoPonto = textDescricaoEstrutura.getText().toString();

                if (descricaoPonto.equals("")) {
                    Toast.makeText(GerenciarLocacoes.this, "Por Favor! Insira a descrição do Ponto!", Toast.LENGTH_SHORT).show();
                } else {
                    //descricaoPonto = descricaoPonto.toUpperCase();

                    db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
                    ContentValues values = new ContentValues();
                    values.put("TIPOPOSTE", "");
                    values.put("ALTURA", "");
                    values.put("CAPACIDADE", "");
                    values.put("NUMPLACA", "");
                    values.put("ILUMICACAOPUBLICA", "");
                    values.put("TELEFONIA", "");
                    values.put("QUANTIDADE", "");
                    values.put("ACESSO", "");
                    values.put("TIPOSOLO", "");
                    values.put("QUANT", descricaoPonto);
                    values.put("IDLOCACAO", idLocacao);

                    db.insert(TABELAPOSTE, null, values);
                    db.close();

                    dialog.dismiss();
                    inflaListaPontos();
                }
            }
        });

        Button dialogButtonCancela = (Button) dialog.findViewById(R.id.alertCancelar);
        dialogButtonCancela.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        // now that the dialog is set up, it's time to show it
        dialog.show();


            /*
        //String numeroPlaca = textNumeroPlaca.getText().toString();
        String estai = textEstai.getText().toString();

        //numeroPlaca = numeroPlaca.toUpperCase();
        estai = estai.toUpperCase();

        int selectedIdIluminacao = radioIluminacao.getCheckedRadioButtonId();
        int selectedIdSolo = radioTipoSolo.getCheckedRadioButtonId();
        int selectedIdPostePodre = radioPostePodre.getCheckedRadioButtonId();
        int selectedIdLinhaViva = radioLinhaViva.getCheckedRadioButtonId();

        String TipoPosteSelecionado = spnTiposPostes.getSelectedItem().toString();
        String CapacidadeSelecionado = spnCapacidade.getSelectedItem().toString();

        Intent intent = new Intent(this, CadastrarExtruturas.class);
        intent.putExtra("USERTELA", "1");
        intent.putExtra("numeroPlaca", String.valueOf(selectedIdLinhaViva));
        intent.putExtra("telefonia1", String.valueOf(radioTelefoniaTL.isChecked()));
        intent.putExtra("telefonia2", String.valueOf(radioTelefoniaGVT.isChecked()));
        intent.putExtra("telefonia3", String.valueOf(radioTelefoniaNET.isChecked()));
        intent.putExtra("telefonia4", String.valueOf(radioTelefoniaFO.isChecked()));
        //intent.putExtra("vidautil", String.valueOf(radioVidaUtil.isChecked()));
        intent.putExtra("estai", estai);
        intent.putExtra("selectedIdAltura", selectedStringAltura);
        intent.putExtra("selectedIdIluminacao", String.valueOf(selectedIdIluminacao));
        intent.putExtra("vidautil", String.valueOf(selectedIdPostePodre));
        intent.putExtra("selectedIdAcesso", selectedIdAcesso);
        intent.putExtra("TipoPosteSelecionado", TipoPosteSelecionado);
        intent.putExtra("CapacidadeSelecionado", CapacidadeSelecionado);
        intent.putExtra("TipoSoloSelecionado", String.valueOf(selectedIdSolo));
        if (valorPagina != null && valorPagina.equals("1")) {
            intent.putExtra("edit", "1");
        }

        this.startActivity(intent);
        finish();
        */
    }

    private void inflaListaDocumentosComIdLocacao() {
        itensDoc = new ArrayList<DadosGerais>();

        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        Cursor linhas = db.rawQuery("SELECT ID, NOMEDOCUMENTO FROM " + TABELADOCUMENTO + " WHERE IDLOCACAO = " + idLocacao + ";", null); //where nota = nota locacao
        int contador = 1;
        if (linhas.moveToFirst()) {
            do {
                String idPoste = linhas.getString(0);
                String texto = contador + " - " + linhas.getString(1);

                DadosGerais itemDoc = new DadosGerais(idPoste, texto);
                itensDoc.add(itemDoc);
                contador++;
            }
            while (linhas.moveToNext());
        }
        adapterListViewDocumentos = new AdapterListViewGeral(this, itensDoc);
        listViewDoc.setAdapter(adapterListViewDocumentos);
        listViewDoc.setCacheColorHint(Color.TRANSPARENT);

        linhas.close();
        Helper.getListViewSize(listViewDoc);
        db.close();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Intent it;
            it = new Intent(GerenciarLocacoes.this, TelaPrincipal.class);
            startActivity(it);
            finish();
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

    public void addLocacao(View v) {
        Bundle bundle = new Bundle();
        Intent it;
        bundle.putString("tipo", "1");
        it = new Intent(this, CadastrarLocacao.class);
        it.putExtras(bundle);
        startActivity(it);
        finish();
    }

    public void editarCliente(View v) {
        if (idLocacao == null) {
            Toast.makeText(GerenciarLocacoes.this, "Por Favor! Cadastre ou selecione uma Locação para edita-lá!", Toast.LENGTH_SHORT).show();
        } else {
            Bundle bundle = new Bundle();
            Intent it;
            bundle.putString("tipo", "2");
            it = new Intent(this, CadastrarLocacao.class);
            it.putExtras(bundle);
            startActivity(it);
            finish();
        }
    }

    public void adicionarPoste(View v) {
        if (idLocacao == null) {
            Toast.makeText(GerenciarLocacoes.this, "Por Favor! Cadastre ou selecione uma Nota!", Toast.LENGTH_SHORT).show();
        } else {
            criarPosteBanco();
            Intent intent = new Intent(this, InseriPoste.class);
            intent.putExtra("USERTELA", "1");
            this.startActivity(intent);
            finish();
        }
    }

    public void criarPosteBanco() {

        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        ContentValues values = new ContentValues();
        values.put("IDLOCACAO", idLocacao);
        values.put("TIPOPOSTE", "");
        values.put("ALTURA", "");
        values.put("CAPACIDADE", "");
        values.put("NUMPLACA", "");
        values.put("ILUMICACAOPUBLICA", "");
        values.put("TELEFONIA", "");
        values.put("QUANTIDADE", "");
        values.put("ACESSO", "");
        values.put("FINALVIDAUTIL", "");
        values.put("TIPOSOLO", "");
        values.put("QUANT", "");

        long ultimoId = db.insert(TABELAPOSTE, null, values);
        String retorno = String.valueOf(ultimoId);
        inseriLocalizacao(retorno);
        SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();

        editor.putString("idPosteKey", retorno);
        editor.commit();
        db.close();

    }

    private void inseriLocalizacao(String retorno) {
        String latitudeBanco = "9999999999";
        String longitudeBanco = "99999999999";
        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);

        db.execSQL("INSERT INTO " + TABELACOORDENADAS + "(LATITUDE, LONGITUDE, IDPOSTE) VALUES (" +
                "'" + latitudeBanco + "','" + longitudeBanco + "','" + retorno + "')");
        db.close();
    }

    public void adicionarDocumento(View v) {
        if (idLocacao == null) {
            Toast.makeText(GerenciarLocacoes.this, "Por Favor! Cadastre ou selecione uma Nota!", Toast.LENGTH_SHORT).show();
        } else {
            Intent it;
            it = new Intent(this, InseriImagemDocumento.class);
            startActivity(it);
            finish();
        }
    }

    public void deletaItem(View v) {
        adapterListViewDocumentos.removeItem((Integer) v.getTag());
        String idMensagem = adapterListViewDocumentos.idSelecionado;
        confirmarDelete(idMensagem);
    }

    public void editarItem(View v) {
        adapterListViewPostes.editaItem((Integer) v.getTag());
        String idMensagem = adapterListViewPostes.idSelecionado;
        editarPoste(idMensagem);
    }

    public void deletaItemPonto(View v) {
        adapterListViewPontos.removeItem((Integer) v.getTag());
        String idMensagem = adapterListViewPontos.idSelecionado;
        confirmarDeletePonto(idMensagem);
    }

    public void editaItemPonto(View v) {

        adapterListViewPontos.editaItem((Integer) v.getTag());
        final String idMensagemteste = adapterListViewPontos.idSelecionado;
        String descricao = null;
        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        Cursor linhas = db.rawQuery("SELECT * FROM " + TABELAPOSTE + " WHERE ID = " + idMensagemteste + ";", null);
        if (linhas.moveToFirst()) {
            do {
                descricao = linhas.getString(11);
            }
            while (linhas.moveToNext());
            linhas.close();
        }
        db.close();

        final Dialog dialog = new Dialog(GerenciarLocacoes.this);
        dialog.setContentView(R.layout.radiocustomizado);

        EditText textDescricaoEstrutura = (EditText) dialog.findViewById(R.id.textDescricaoEstrutura);
        textDescricaoEstrutura.setText(descricao);
        dialog.setTitle("Edição de Ponto");
        dialog.setCancelable(true);

        Button dialogButton = (Button) dialog.findViewById(R.id.alertOK);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String descricaoEstru = "";
                EditText textDescricaoEstrutura = (EditText) dialog.findViewById(R.id.textDescricaoEstrutura);
                descricaoEstru = textDescricaoEstrutura.getText().toString();

                if (descricaoEstru.equals("")) {
                    Toast.makeText(GerenciarLocacoes.this, "Por Favor! Insira a descrição do ponto!", Toast.LENGTH_SHORT).show();
                } else {
                    //descricaoEstru = descricaoEstru.toUpperCase();
                    db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
                    ContentValues values = new ContentValues();
                    values.put("QUANT", descricaoEstru);
                    db.update(TABELAPOSTE, values, "ID=" + idMensagemteste, null);
                    db.close();

                    dialog.dismiss();
                    inflaListaPontos();
                    inflaListaPostes();
                }
            }
        });

        Button dialogButtonCancela = (Button) dialog.findViewById(R.id.alertCancelar);
        dialogButtonCancela.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        // now that the dialog is set up, it's time to show it
        dialog.show();


    }

    private void confirmarDelete(final String idMensagem) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Tem certeza que deseja deletar este Documento?");

        alertDialogBuilder.setPositiveButton("Sim",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        deletarMensagem(idMensagem);
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

    private void confirmarDeletePonto(final String idMensagem) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Tem certeza que deseja deletar este Ponto?");

        alertDialogBuilder.setPositiveButton("Sim",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        deletarPonto(idMensagem);
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

    private void deletarMensagem(final String idMens) {
        int idExcluido = Integer.parseInt(idMens.toString());
        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        db.execSQL("DELETE FROM " + TABELADOCUMENTO + " WHERE ID = " + idExcluido + "");
        db.close();

        inflaListaDocumentosComIdLocacao();
    }

    private void deletarPonto(final String idMens) {
        int idExcluido = Integer.parseInt(idMens.toString());
        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        db.execSQL("DELETE FROM " + TABELAPOSTE + " WHERE ID = " + idExcluido + "");
        db.close();

        inflaListaPontos();
        inflaListaPostes();
    }

    private void editarPoste(final String idMens) {
        SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();

        editor.putString("idPosteKey", idMens);
        editor.commit();

        Intent intent = new Intent(this, InseriPoste.class);
        intent.putExtra("USERTELA", "EDITAR");
        this.startActivity(intent);
        finish();
    }

    public void salvarLocacao(View v) {
        finalizarLocacao();
    }

    private void finalizarLocacao() {
        if (idLocacao == null) {
            Toast.makeText(GerenciarLocacoes.this, "Por Favor! Cadastre ou selecione uma Locação!", Toast.LENGTH_SHORT).show();
        } else {
            progress = ProgressDialog.show(GerenciarLocacoes.this, "Geração de Arquivos",
                    "Os Arquivos estão sendo gerados, Aguarde!", true);

            new Thread(new Runnable() {
                @Override
                public void run() {

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    Date data = new Date();

                    Calendar cal = Calendar.getInstance();
                    cal.setTime(data);
                    Date data_atual = cal.getTime();
                    String data_completa = dateFormat.format(data_atual);

                    db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
                    ContentValues values = new ContentValues();
                    values.put("STATUS", "1");
                    values.put("DATA", data_completa);
                    db.update(TABELALOCACAO, values, "ID=" + idLocacao, null);
                    db.close();

                    criaTxtPoste(idLocacao);
                    criaTxtConsumidores(idLocacao);
                    try {
                        createPdfWrapper();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (DocumentException e) {
                        e.printStackTrace();
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progress.dismiss();
                            sucessoGeracaoArquivos();
                        }
                    });
                }
            }).start();
        }
    }

    public void sucessoGeracaoArquivos() {
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Sucesso na Geração de Arquivos");
        alertDialogBuilder.setMessage("Os arquivos TXTs e PDFs foram gerados com sucesso!");

        alertDialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.remove("numeroNotaKey");
                        editor.remove("idLocacaoKey");

                        editor.commit();
                        editor.clear();

                        Intent it;
                        it = new Intent(GerenciarLocacoes.this, TelaPrincipal.class);
                        startActivity(it);
                        finish();
                    }
                });


        android.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void criaTxtPoste(String idLocacao) {
        String textoFinal = "";
        String valorNulo = "1";

        String textoFinalEstruturaPrimaria = "";
        String textoFinalEstruturaSecundaria = "";
        String id = "";

        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        Cursor linhas = db.rawQuery("SELECT * FROM " + TABELAPOSTE + " WHERE IDLOCACAO = " + idLocacao + ";", null);

        int contador = 1;
        if (linhas.moveToFirst()) {
            do {
                if (contador < 10) {
                    id = "00" + contador;
                } else if (contador >= 10 && contador < 100) {
                    id = "0" + contador;
                } else if (contador >= 100) {
                    id = String.valueOf(contador);
                }

                if (!linhas.getString(11).equals("")) {
                    String numero = String.format("%-3s", id);
                    String coordenadaX = String.format("%-10s", "9999999999");
                    String coordenadaY = String.format("%-11s", "99999999999");
                    String alturaCapacidade = String.format("%-7s", "-");
                    String codigo = String.format("%-10s", "-");
                    String bloco = String.format("%-4s", "PON");
                    String iluminacaoPublica = String.format("%-3s", "Não");
                    String solo = String.format("%-1s", "-");
                    String acesso = String.format("%-1s", "-");
                    String vidaUtil = String.format("%-3s", "-");
                    String estruturaMT = String.format("%-20s", linhas.getString(11));
                    String estruturaBT = String.format("%-20s", "-");
                    String estai = "-";
                    String tlSemelhantes = "-";

                    texto = (numero + " " + coordenadaX + " " + coordenadaY + " " + alturaCapacidade + " " + estruturaMT + " " + estruturaBT + " " + String.format("%-15s", estai) + " " + codigo + " " + bloco + " " + iluminacaoPublica + " " + String.format("%-13s", tlSemelhantes) + " " + solo + " " + acesso + " " + vidaUtil + " \n");

                    estai = "";
                    tlSemelhantes = "";
                    //Log.d("teste","Vai planeta1: "+texto);
                } else {


                    Cursor linhasCoordenadas = db.rawQuery("SELECT * FROM " + TABELACOORDENADAS + " WHERE IDPOSTE = " + linhas.getString(0) + ";", null);

                    String latitudeUTM = "";
                    String longitudeUTM = "";

                    if (linhasCoordenadas.moveToFirst()) {
                        do {
                            latitudeUTM = linhasCoordenadas.getString(2);
                            longitudeUTM = linhasCoordenadas.getString(1);

                        }
                        while (linhasCoordenadas.moveToNext());
                        linhasCoordenadas.close();
                    }

                    Cursor linhasEstrutura = db.rawQuery("SELECT * FROM " + TABELAESTRUTURA + " WHERE IDPOSTE = " + linhas.getString(0) + ";", null);

                    if (linhasEstrutura.moveToFirst()) {
                        do {
                            if (linhasEstrutura.getString(1).equals("Primaria")) {
                                textoEstruturaPrimaria = (linhasEstrutura.getString(2));
                                textoFinalEstruturaPrimaria = textoFinalEstruturaPrimaria + textoEstruturaPrimaria;
                            } else if (linhasEstrutura.getString(1).equals("Secundaria")) {
                                textoEstruturaSecundaria = (linhasEstrutura.getString(2));
                                textoFinalEstruturaSecundaria = textoFinalEstruturaSecundaria + textoEstruturaSecundaria;
                            }
                            textoEstruturaPrimaria = "";
                            textoEstruturaSecundaria = "";
                        }
                        while (linhasEstrutura.moveToNext());
                        linhasEstrutura.close();
                    }
                    String AlturaCapacidade = "";

                    if (linhas.getString(2) != null && !linhas.getString(3).equals("")) {
                        AlturaCapacidade = linhas.getString(2) + "-" + linhas.getString(3);
                    } else if (linhas.getString(2) != null && linhas.getString(3).equals("")) {
                        AlturaCapacidade = linhas.getString(2);
                    } else if (linhas.getString(2) == null && !linhas.getString(3).equals("")) {
                        AlturaCapacidade = linhas.getString(3);
                    } else if (linhas.getString(2) == null && linhas.getString(3).equals("")) {
                        AlturaCapacidade = "";
                    }
                    if (latitudeUTM.equals("") && longitudeUTM.equals("") && AlturaCapacidade.equals("") && textoFinalEstruturaPrimaria.equals("") && textoFinalEstruturaSecundaria.equals("") && linhas.getString(7).equals("") && linhas.getString(4).equals("") && linhas.getString(1).equals("") && linhas.getString(6).equals("") && linhas.getString(9).equals("") && linhas.getString(8).equals("")) {
                        valorNulo = "1";
                    } else {
                        valorNulo = "2";
                    }

                    String numero = String.format("%-3s", id);
                    String coordenadaX = String.format("%-10s", latitudeUTM);
                    String coordenadaY = String.format("%-11s", longitudeUTM);
                    String alturaCapacidade = String.format("%-7s", AlturaCapacidade);
                    String codigo = String.format("%-10s", linhas.getString(4));
                    String bloco = String.format("%-4s", linhas.getString(1));
                    String iluminacaoPublica = String.format("%-3s", linhas.getString(5));
                    String solo = String.format("%-1s", linhas.getString(9));
                    String acesso = String.format("%-1s", linhas.getString(8));
                    String vidaUtil = String.format("%-3s", linhas.getString(10));
                    String estai;
                    String tlSemelhantes;

                    if (valorNulo.equals("1")) {
                        texto = ("");
                    } else {
                        if (textoFinalEstruturaPrimaria.equals("")) {
                            textoFinalEstruturaPrimaria = "-";
                        }
                        if (textoFinalEstruturaSecundaria.equals("")) {
                            textoFinalEstruturaSecundaria = "-";
                        }

                        if (linhas.getString(6).equals("")) {
                            tlSemelhantes = "-";
                        } else {
                            tlSemelhantes = linhas.getString(6);
                        }

                        if (linhas.getString(7).equals("")) {
                            estai = "-";
                        } else {
                            estai = linhas.getString(7);
                        }
                        String estruturaMT = String.format("%-20s", textoFinalEstruturaPrimaria);
                        String estruturaBT = String.format("%-20s", textoFinalEstruturaSecundaria);

                        texto = (numero + " " + coordenadaX + " " + coordenadaY + " " + alturaCapacidade + " " + estruturaMT + " " + estruturaBT + " " + String.format("%-15s", estai) + " " + codigo + " " + bloco + " " + iluminacaoPublica + " " + String.format("%-13s", tlSemelhantes) + " " + solo + " " + acesso + " " + vidaUtil + " \n");
                        Log.d("teste","Vai planeta2: "+texto);
                    }
                    estai = "";
                    tlSemelhantes = "";
                }
                textoFinal = textoFinal + texto;
                textoFinalEstruturaSecundaria = "";
                textoFinalEstruturaPrimaria = "";
                contador++;
            }
            while (linhas.moveToNext());
            linhas.close();
        }
        db.close();
        if (textoFinal.equals("")) {
            textoFinal = "Não foram inseridos dados de nenhum poste neste Relatório!";
        }
        gerarArquivo(this, notaLocacao + "_Postes.txt", textoFinal);
    }

    public void criaTxtConsumidores(String idLocacao) {
        String textoFinal = "";
        String texto1 = "";
        String texto2 = "";
        String textoSegundoRelatorio = "";
        String textoFinalSegundoRelatorio = "";
        String id = "";
        String idPoste = "";

        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        Cursor linhas = db.rawQuery("SELECT * FROM " + TABELAPOSTE + " WHERE IDLOCACAO = " + idLocacao + ";", null);

        int contador = 1;
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

                Cursor linhas2 = db.rawQuery("SELECT * FROM " + TABELACONSUMIDOR + " WHERE IDPOSTE = " + idPoste + ";", null);

                int contador2 = 0;
                if (linhas2.moveToFirst()) {
                    do {
                        texto2 = (id + " " + linhas2.getString(1) + " " + String.format("%-25s", linhas2.getString(4)) + " \n");
                        textoFinal = textoFinal + texto2;
                        contador2++;
                    }
                    while (linhas2.moveToNext());
                    linhas2.close();
                }
                textoSegundoRelatorio = id + " " + contador2 + " " + linhas.getString(5);
                textoFinalSegundoRelatorio = textoFinalSegundoRelatorio + textoSegundoRelatorio + " \n";
                texto2 = "";
                contador++;
            }
            while (linhas.moveToNext());
            linhas.close();
        }
        db.close();

        if (textoFinal.equals("")) {
            textoFinal = "Não foram inseridos dados sobre os Consumidores neste relatório!";
        }
        gerarArquivo(this, notaLocacao + "_Consumidores.txt", textoFinal);
        gerarArquivo(this, notaLocacao + "_Queda.txt", textoFinalSegundoRelatorio);
    }

    public void gerarArquivo(Context context, String nomeArquivo, String conteudo) {
        if (conteudo.equals("")) {
            conteudo = "Não foram inseridos dados!";
        }

        try {
            File root = new File(Environment.getExternalStorageDirectory(), "HCC");
            if (!root.exists()) {
                root.mkdirs();
            }

            File root2 = new File(Environment.getExternalStorageDirectory(), "HCC/" + notaLocacao);
            if (!root2.exists()) {
                root2.mkdirs();
            }

            File gpxfile = new File(root2, nomeArquivo);
            //FileWriter writer = new FileWriter(gpxfile);
            //writer.append(conteudo);
            //writer.flush();
            //writer.close();

            //OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(gpxfile, true),
            //"Cp1252");

            Writer out = new OutputStreamWriter(new FileOutputStream(gpxfile), "windows-1252");
            out.append(conteudo);
            out.flush();
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // verifica permissões
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

    // Metodo responsavel por verificar se as permissões foram aceitas
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) { // Se a permissão foi aceita, chama o metodo para criar o PDF
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

    // Metodo do Alert
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    // Cria PDF com inserções de imagens dos postes.
    private void createPdf() throws IOException, DocumentException {

        int contador = 1;
        String idPoste = "", conferiTexto = "", textoDadosEquipamento = "", id = "";

        // Verifica se a pasta HCC já existe no smartfone, se não existir, cria a pasta
        File docsFolder = new File(Environment.getExternalStorageDirectory() + "/HCC");
        if (!docsFolder.exists()) {
            docsFolder.mkdir();
        }

        // Verifica se a pasta com o numero da nota ja existe dentro da pasta HCC
        // Se não existir, cria a nova pasta
        File docsFolder2 = new File(Environment.getExternalStorageDirectory() + "/HCC/" + notaLocacao);
        if (!docsFolder2.exists()) {
            docsFolder2.mkdir();
        }

        // Criando o arquivo PDF, com o n da nota como nome do arquivo
        pdfFile = new File(docsFolder2.getAbsolutePath(), notaLocacao + "_Equipamento.pdf");
        OutputStream output = new FileOutputStream(pdfFile);
        Document document = new Document(PageSize.A4, 0, 0, 0, 0);
        PdfWriter.getInstance(document, output);

        document.addTitle("Equipamentos");

        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        Cursor linhas = db.rawQuery("SELECT * FROM " + TABELAPOSTE + " WHERE IDLOCACAO = " + idLocacao + ";", null);
        if (linhas.moveToFirst()) {
            document.open();
            do {
                idPoste = linhas.getString(0);
                // Contador que controla a quantidade de imagens
                if (contador < 10) {
                    id = "00" + contador;
                } else if (contador >= 10 && contador < 100) {
                    id = "0" + contador;
                } else if (contador >= 100) {
                    id = "" + String.valueOf(contador);
                }

                // Select que busca os dados da locação
                Cursor linhas2 = db.rawQuery("SELECT * FROM " + TABELAEQUIPAMENTO + " WHERE IDPOSTE= " + idPoste + ";", null);
                if (linhas2.moveToFirst()) {
                    do {
                        // Caso não existirem dados daquele poste
                        if (linhas2.getString(1).equals("") && linhas2.getString(2).equals("")) {
                            textoDadosEquipamento = "Não foram inseridos dados do Equipamento!";
                            conferiTexto = "1";
                        } else {
                            textoDadosEquipamento = "   Tipo de Equipamento: " + linhas2.getString(1) + "\n   Numero da Placa: " + linhas2.getString(2) + "\n\n";
                            conferiTexto = "0";
                        }

                        // Select para buscar as imagens referentes ao id daquele poste
                        Cursor linhas3 = db.rawQuery("SELECT * FROM " + TABELAFOTOSPOSTE + " WHERE IDPOSTE= " + idPoste + ";", null);

                        if (linhas3.moveToFirst() && linhas3.getCount() > 0) {
                            int contador2 = 1;
                            do {
                                byte[] imagemBase64 = linhas3.getBlob(1);
                                if (imagemBase64 != null) {
                                    contador2++;

                                    // Cria a parte de cima da pagina, com a imagem padrão
                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                    Bitmap bitmap = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.moldecima);
                                    Bitmap tamanhoReduzidoParaCima = Bitmap.createScaledBitmap(bitmap, 589, 80, true);
                                    tamanhoReduzidoParaCima.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                    Image myImg = Image.getInstance(stream.toByteArray());
                                    myImg.setAlignment(Image.MIDDLE);
                                    document.add(myImg);

                                    ByteArrayOutputStream stream3 = new ByteArrayOutputStream();
                                    Bitmap imagemBitmap = decodeBase64(imagemBase64);
                                    Bitmap novoTamanho = Bitmap.createScaledBitmap(imagemBitmap, (int) (imagemBitmap.getWidth() * 0.38), (int) (imagemBitmap.getHeight() * 0.38), true);//tava 0.68
                                    novoTamanho.compress(Bitmap.CompressFormat.JPEG, 100, stream3);
                                    Image myImg3 = Image.getInstance(stream3.toByteArray());


                                    document.add(new Paragraph(" Poste: " + id));
                                    Paragraph paragrafo1 = new Paragraph(textoDadosEquipamento);
                                    //inseri paragrafo com os dados do poste
                                    document.add(paragrafo1);

                                    myImg3.setAlignment(Image.MIDDLE);
                                    myImg3.setAbsolutePosition(132f, 190f);
                                    // Inseri paragrafo com imagens do poste
                                    document.add(myImg3);

                                    // Cria a parte de baixo da pagina, com a imagem padrão
                                    ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
                                    Bitmap bitmap2 = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.moldebaixo);
                                    Bitmap tamanhoReduzidoParaBaixo = Bitmap.createScaledBitmap(bitmap2, 589, 190, true);
                                    tamanhoReduzidoParaBaixo.compress(Bitmap.CompressFormat.JPEG, 100, stream2);
                                    Image myImg2 = Image.getInstance(stream2.toByteArray());
                                    myImg2.setAbsolutePosition(3, 3);
                                    document.add(myImg2);
                                    document.newPage();

                                }
                                document.newPage();
                            }
                            while (linhas3.moveToNext());
                            linhas3.close();
                        } else {

                            if (conferiTexto.equals("0")) {
                                // Cria a parte de cima da pagina, com a imagem padrão
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                Bitmap bitmap = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.moldecima);
                                Bitmap tamanhoReduzidoParaCima = Bitmap.createScaledBitmap(bitmap, 589, 80, true);
                                tamanhoReduzidoParaCima.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                Image myImg = Image.getInstance(stream.toByteArray());
                                myImg.setAlignment(Image.MIDDLE);
                                document.add(myImg);

                                // Inseri paragrafo com os dados do poste
                                document.add(new Paragraph(" Poste: " + id));
                                Paragraph paragrafo1 = new Paragraph(textoDadosEquipamento);
                                document.add(paragrafo1);

                                // Inseri outro paragrafo com o feedback
                                Paragraph paragrafo2 = new Paragraph(" Não foi inserida imagem deste Equipamento");
                                paragrafo2.setAlignment(Element.ALIGN_CENTER);

                                // Cria a parte de baixo da pagina, com a imagem padrão
                                ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
                                Bitmap bitmap2 = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.moldebaixo);
                                Bitmap tamanhoReduzidoParaBaixo = Bitmap.createScaledBitmap(bitmap2, 589, 190, true);
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
    }

    // Cria o PDF de todas as Imagens dos documentos, retiradas da Locação
    public void createPdfImagens() throws IOException, DocumentException {

        // Verifica se a pasta HCC já existe no smartfone, se não existir, cria a pasta
        File docsFolder = new File(Environment.getExternalStorageDirectory() + "/HCC");
        if (!docsFolder.exists()) {
            docsFolder.mkdir();
        }

        // Verifica se a pasta com o numero da nota ja existe dentro da pasta HCC
        // Se não existir, cria a nova pasta
        File docsFolder2 = new File(Environment.getExternalStorageDirectory() + "/HCC/" + notaLocacao);
        if (!docsFolder2.exists()) {
            docsFolder2.mkdir();
        }

        // Criando o arquivo PDF, com o n da nota como nome do arquivo
        pdfFile = new File(docsFolder2.getAbsolutePath(), notaLocacao + "_Imagens.pdf");
        OutputStream output = new FileOutputStream(pdfFile);
        Document document = new Document(PageSize.A4, 0, 0, 0, 0);
        PdfWriter.getInstance(document, output);

        document.addTitle("Imagens");

        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);

        // select que busca todas as imagens e nomes de documentos retirados daquela locação
        Cursor linhas = db.rawQuery("SELECT ID, NOMEDOCUMENTO, IMAGEM FROM " + TABELADOCUMENTO + " WHERE IDLOCACAO = " + idLocacao + ";", null);

        int contadorPassagem = 0, contadorCliente = 0, contadorProjeto = 0, contadorCroqui = 0;
        int contadorAcompanhamento = 0, contadorAmbiental = 0, contadorGarantia = 0;

        if (linhas.moveToFirst()) { // Caso exista pelo menos uma imagem nesta locação
            // Para cada imagem cria-se uma pagina no PDF
            document.open();
            do {
                // Cria a parte de cima da pagina
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                Bitmap bitmap = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.moldecima);
                Bitmap tamanhoReduzidoParaCima = Bitmap.createScaledBitmap(bitmap, 589, 80, true);
                tamanhoReduzidoParaCima.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                Image myImg = Image.getInstance(stream.toByteArray());
                myImg.setAlignment(Image.MIDDLE);

                document.add(myImg);

                // Primero paragrafo
                Font titleFont = new Font(Font.FontFamily.COURIER, 20, Font.BOLD);
                Paragraph paragrafo1 = new Paragraph("RELATÓRIO FOTOGRÁFICO\n");
                paragrafo1.setFont(titleFont);
                paragrafo1.setAlignment(Element.ALIGN_CENTER);
                document.add(paragrafo1);

                String nomeDocumento = linhas.getString(1);
                Paragraph paragrafo2 = new Paragraph("  " + nomeDocumento + "\n");
                document.add(paragrafo2);
                Paragraph paragrafo3 = new Paragraph("\n");


                // Imagem em base64 inserida na sua variavél
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

                    // Converte a base64 para bitmap, configurando a imagem(tamanho, alinhamento, etc)
                    ByteArrayOutputStream stream3 = new ByteArrayOutputStream();
                    Bitmap imagemBitmap = decodeBase64(imagemBase64);
                    Bitmap novoTamanho = Bitmap.createScaledBitmap(imagemBitmap, (int) (imagemBitmap.getWidth() * 0.53), (int) (imagemBitmap.getHeight() * 0.53), true);// tava 0.71
                    novoTamanho.compress(Bitmap.CompressFormat.JPEG, 100, stream3);
                    Image myImg3 = Image.getInstance(stream3.toByteArray());
                    myImg3.setAlignment(Image.ALIGN_CENTER);

                    // Inseri imagem como segundo paragrafo
                    document.add(myImg3);
                    document.add(paragrafo3);
                }
                document.newPage();
            }
            while (linhas.moveToNext());
            linhas.close();
            document.close();
        } else { // Caso NÃO exista nenhuma imagem para esta locação

            // cria a pagina em PDF
            document.open();

            // Inseri a imagem da parte de cima da pagina
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Bitmap bitmap = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.moldecima);
            Bitmap tamanhoReduzidoParaCima = Bitmap.createScaledBitmap(bitmap, 589, 80, true);
            tamanhoReduzidoParaCima.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            Image myImg = Image.getInstance(stream.toByteArray());
            myImg.setAlignment(Image.MIDDLE);
            document.add(myImg);

            // Inseri o primeiro paragrafo
            Font titleFont = new Font(Font.FontFamily.COURIER, 20, Font.BOLD);
            Paragraph paragrafo1 = new Paragraph("Não foi inserida nenhuma imagem de documentos neste relatório!\n");
            paragrafo1.setFont(titleFont);
            paragrafo1.setAlignment(Element.ALIGN_CENTER);
            document.add(paragrafo1);

            // Inseri a imagem da parte de baixo da imagem
            ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
            Bitmap bitmap2 = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.moldebaixo);
            Bitmap tamanhoReduzidoParaBaixo = Bitmap.createScaledBitmap(bitmap2, 589, 190, true);
            tamanhoReduzidoParaBaixo.compress(Bitmap.CompressFormat.JPEG, 100, stream2);
            Image myImg2 = Image.getInstance(stream2.toByteArray());
            myImg2.setAbsolutePosition(3, 3);
            document.add(myImg2);

            document.close();
        }
        db.close();
    }

    // Metodo que decodifica para base64
    public static Bitmap decodeBase64(byte[] input) {
        byte[] decodedBytes = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

}
