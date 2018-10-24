package br.desenvolvedor.michelatz.aplicativohcc;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import br.desenvolvedor.michelatz.aplicativohcc.Adapter.AdapterListViewConsumidores;
import br.desenvolvedor.michelatz.aplicativohcc.ClassesExtras.Helper;
import br.desenvolvedor.michelatz.aplicativohcc.Modelo.DadosGerais;

public class InseriPoste extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Spinner spnTiposPostes, spnCapacidade;
    private RadioGroup radioAltura, radioIluminacao, radioAcesso, radioTipoSolo;
    private RadioButton bt8, bt9, bt10, bt11, bt12, bt13, bt14, bt15, bt16, bt17, bt18, btSim, btNao, btA, btB, btC;
    private RadioButton btAcesso1, btAcesso2, btAcesso3, btAcesso4, btAcesso5, btAcesso6, btAcesso7, btAcesso8;
    private CheckBox radioTelefoniaTL, radioTelefoniaNET, radioTelefoniaFO, radioVidaUtil;
    private ListView listViewEstruturas;
    private TextView textNumeroPlaca, textEstai;

    private String selectedIdAcesso = "", selectedStringAltura = " ";
    private String idLocacao, idPoste, valorPagina;
    String BANCO = "banco.db", TABELAPOSTE = "poste", TABELAESTRUTURA = "estrutura";

    private AdapterListViewConsumidores adapterListViewConsumidores;
    ArrayList<String> tiposposte = new ArrayList<String>();
    ArrayList<String> capacidade = new ArrayList<String>();
    ArrayList<String> tipoSolo = new ArrayList<String>();

    SQLiteDatabase db;
    ArrayAdapter adapter, adapter2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inseri_poste);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setTitle("Aplicativo HCC");
        toolbar.setSubtitle("Dados do Poste");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
        idLocacao = sharedpreferences.getString("idLocacaoKey", null);
        idPoste = sharedpreferences.getString("idPosteKey", null);

        listViewEstruturas = (ListView) findViewById(R.id.listViewEstruturas);

        textNumeroPlaca = (TextView) findViewById(R.id.textNumeroPlaca);
        textEstai = (TextView) findViewById(R.id.textEstai);

        radioAltura = (RadioGroup) findViewById(R.id.radioAltura);
        radioIluminacao = (RadioGroup) findViewById(R.id.radioIluminacao);
        radioAcesso = (RadioGroup) findViewById(R.id.radioAcesso);
        radioTipoSolo = (RadioGroup) findViewById(R.id.radioTipoSolo);


        btSim = (RadioButton) findViewById(R.id.btSim);
        btNao = (RadioButton) findViewById(R.id.btNao);

        btA = (RadioButton) findViewById(R.id.btA);
        btB = (RadioButton) findViewById(R.id.btB);
        btC = (RadioButton) findViewById(R.id.btC);

        btAcesso1 = (RadioButton) findViewById(R.id.btAcesso1);
        btAcesso2 = (RadioButton) findViewById(R.id.btAcesso2);
        btAcesso3 = (RadioButton) findViewById(R.id.btAcesso3);
        btAcesso4 = (RadioButton) findViewById(R.id.btAcesso4);
        btAcesso5 = (RadioButton) findViewById(R.id.btAcesso5);
        btAcesso6 = (RadioButton) findViewById(R.id.btAcesso6);
        btAcesso7 = (RadioButton) findViewById(R.id.btAcesso7);
        btAcesso8 = (RadioButton) findViewById(R.id.btAcesso8);

        bt8 = (RadioButton) findViewById(R.id.bt8);
        bt9 = (RadioButton) findViewById(R.id.bt9);
        bt10 = (RadioButton) findViewById(R.id.bt10);
        bt11 = (RadioButton) findViewById(R.id.bt11);
        bt12 = (RadioButton) findViewById(R.id.bt12);
        bt13 = (RadioButton) findViewById(R.id.bt13);
        bt14 = (RadioButton) findViewById(R.id.bt14);
        bt15 = (RadioButton) findViewById(R.id.bt15);
        bt16 = (RadioButton) findViewById(R.id.bt16);
        bt17 = (RadioButton) findViewById(R.id.bt17);
        bt18 = (RadioButton) findViewById(R.id.bt18);

        radioTelefoniaTL = (CheckBox) findViewById(R.id.radioTelefoniaTL);
        radioTelefoniaNET = (CheckBox) findViewById(R.id.radioTelefoniaNET);
        radioTelefoniaFO = (CheckBox) findViewById(R.id.radioTelefoniaFO);

        radioVidaUtil = (CheckBox) findViewById(R.id.radioVidaUtil);

        tiposposte.add("Selecione um Tipo");
        tiposposte.add("PMAE");
        tiposposte.add("PMAI");
        tiposposte.add("PDTE");
        tiposposte.add("PDTI");
        tiposposte.add("PCOE");
        tiposposte.add("PCOI");
        tiposposte.add("PFBE");
        tiposposte.add("PFBI");

        capacidade.add("Selecione a capacidade");
        capacidade.add("150");
        capacidade.add("200");
        capacidade.add("300");
        capacidade.add("400");
        capacidade.add("600");
        capacidade.add("800");
        capacidade.add("1000");
        capacidade.add("1200");
        capacidade.add("1500");
        capacidade.add("2000");

        tipoSolo.add("Selecione o tipo de solo");
        tipoSolo.add("A - Fácil escavação");
        tipoSolo.add("B - Pequenos pedregulhos");
        tipoSolo.add("C - Solo rochoso");

        spnTiposPostes = (Spinner) findViewById(R.id.spnTiposPostes);
        spnCapacidade = (Spinner) findViewById(R.id.spnCapacidade);

        adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, tiposposte);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnTiposPostes.setAdapter(adapter);

        adapter2 = new ArrayAdapter(this, android.R.layout.simple_spinner_item, capacidade);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCapacidade.setAdapter(adapter2);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (getIntent().getStringExtra("USERTELA") != null) {
            if (getIntent().getStringExtra("USERTELA").equals("5")) {
                if (getIntent().getStringExtra("selectedIdAcesso") != null) {
                    if (bundle.getString("selectedIdAcesso").equals("1")) {
                        selectedIdAcesso = "1";
                        radioAcesso.check(R.id.btAcesso1);
                    } else if (bundle.getString("selectedIdAcesso").equals("2")) {
                        selectedIdAcesso = "2";
                        radioAcesso.check(R.id.btAcesso2);
                    } else if (bundle.getString("selectedIdAcesso").equals("3")) {
                        selectedIdAcesso = "3";
                        radioAcesso.check(R.id.btAcesso3);
                    } else if (bundle.getString("selectedIdAcesso").equals("4")) {
                        selectedIdAcesso = "4";
                        radioAcesso.check(R.id.btAcesso4);
                    } else if (bundle.getString("selectedIdAcesso").equals("5")) {
                        selectedIdAcesso = "5";
                        radioAcesso.check(R.id.btAcesso5);
                    } else if (bundle.getString("selectedIdAcesso").equals("6")) {
                        selectedIdAcesso = "6";
                        radioAcesso.check(R.id.btAcesso6);
                    } else if (bundle.getString("selectedIdAcesso").equals("7")) {
                        selectedIdAcesso = "7";
                        radioAcesso.check(R.id.btAcesso7);
                    } else if (bundle.getString("selectedIdAcesso").equals("8")) {
                        selectedIdAcesso = "8";
                        radioAcesso.check(R.id.btAcesso8);
                    }
                }

                if (getIntent().getStringExtra("selectedIdAltura") != null) {
                    if (bundle.getString("selectedIdAltura").equals("8")) {
                        selectedStringAltura = "8";
                        bt8.setChecked(true);
                        bt8.setTextColor(Color.RED);
                    } else if (bundle.getString("selectedIdAltura").equals("9")) {
                        selectedStringAltura = "9";
                        bt9.setChecked(true);
                        bt9.setTextColor(Color.RED);
                    } else if (bundle.getString("selectedIdAltura").equals("10")) {
                        selectedStringAltura = "10";
                        bt10.setChecked(true);
                        bt10.setTextColor(Color.RED);
                    } else if (bundle.getString("selectedIdAltura").equals("11")) {
                        selectedStringAltura = "11";
                        bt11.setChecked(true);
                        bt11.setTextColor(Color.RED);
                    } else if (bundle.getString("selectedIdAltura").equals("12")) {
                        selectedStringAltura = "12";
                        bt12.setChecked(true);
                        bt12.setTextColor(Color.RED);
                    } else if (bundle.getString("selectedIdAltura").equals("13")) {
                        selectedStringAltura = "13";
                        bt13.setChecked(true);
                        bt13.setTextColor(Color.RED);
                    } else if (bundle.getString("selectedIdAltura").equals("14")) {
                        selectedStringAltura = "14";
                        bt14.setChecked(true);
                        bt14.setTextColor(Color.RED);
                    } else if (bundle.getString("selectedIdAltura").equals("15")) {
                        selectedStringAltura = "15";
                        bt15.setChecked(true);
                        bt15.setTextColor(Color.RED);
                    } else if (bundle.getString("selectedIdAltura").equals("16")) {
                        selectedStringAltura = "16";
                        bt16.setChecked(true);
                        bt16.setTextColor(Color.RED);
                    } else if (bundle.getString("selectedIdAltura").equals("17")) {
                        selectedStringAltura = "17";
                        bt17.setChecked(true);
                        bt17.setTextColor(Color.RED);
                    } else if (bundle.getString("selectedIdAltura").equals("18")) {
                        selectedStringAltura = "18";
                        bt18.setChecked(true);
                        bt18.setTextColor(Color.RED);
                    }
                }

                if (getIntent().getStringExtra("selectedIdIluminacao") != null) {
                    int iluminacao = Integer.parseInt(bundle.getString("selectedIdIluminacao"));
                    radioIluminacao.check(iluminacao);
                }

                if (getIntent().getStringExtra("TipoSoloSelecionado") != null) {
                    int solo = Integer.parseInt(bundle.getString("TipoSoloSelecionado"));
                    radioTipoSolo.check(solo);
                }

                if (getIntent().getStringExtra("numeroPlaca") != null) {
                    textNumeroPlaca.setText(bundle.getString("numeroPlaca"));
                }

                if (getIntent().getStringExtra("telefonia1").equals("true")) {
                    radioTelefoniaTL.setChecked(true);
                }

                if (getIntent().getStringExtra("telefonia3").equals("true")) {
                    radioTelefoniaNET.setChecked(true);
                }

                if (getIntent().getStringExtra("telefonia4").equals("true")) {
                    radioTelefoniaFO.setChecked(true);
                }

                if (getIntent().getStringExtra("estai") != null) {
                    textEstai.setText(bundle.getString("estai"));
                }

                if (getIntent().getStringExtra("TipoPosteSelecionado") != null) {
                    int spinnerPosition = adapter.getPosition(bundle.getString("TipoPosteSelecionado"));
                    spnTiposPostes.setSelection(spinnerPosition);
                }

                if (getIntent().getStringExtra("CapacidadeSelecionado") != null) {
                    int spinnerPosition2 = adapter2.getPosition(bundle.getString("CapacidadeSelecionado"));
                    spnCapacidade.setSelection(spinnerPosition2);
                }

                if (getIntent().getStringExtra("vidautil").equals("true")) {
                    radioVidaUtil.setChecked(true);
                }

                if (getIntent().getStringExtra("edit") != null && getIntent().getStringExtra("edit").equals("1")) {
                    valorPagina = "1";
                }
            } else if (getIntent().getStringExtra("USERTELA").equals("EDITAR")) {
                valorPagina = "1";
                preencheDadosEdicao();
            }
        }
        inflaListaEstruturasComIdLocacao();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //Botão adicional na ToolBar
        switch (item.getItemId()) {
            case android.R.id.home:  //ID do seu botão (gerado automaticamente pelo android, usando como está, deve funcionar
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setMessage("Tem certeza que deseja sair desta aba? Os dados ainda não foram salvos");

                alertDialogBuilder.setPositiveButton("Sim",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.remove("idPosteKey");

                                editor.commit();
                                editor.clear();
                                Intent intent = new Intent(InseriPoste.this, GerenciarLocacoes.class);
                                InseriPoste.this.startActivity(intent);
                                finish();
                                InseriPoste.super.onBackPressed();
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
            default:
                break;
        }
        return true;
    }

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
                            SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.remove("idPosteKey");

                            editor.commit();
                            editor.clear();
                            Intent intent = new Intent(InseriPoste.this, GerenciarLocacoes.class);
                            InseriPoste.this.startActivity(intent);
                            finish();
                            InseriPoste.super.onBackPressed();
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

    public void adicionarExtrutura(View v) {

        String numeroPlaca = textNumeroPlaca.getText().toString();
        String estai = textEstai.getText().toString();

        numeroPlaca = numeroPlaca.toUpperCase();
        estai = estai.toUpperCase();

        int selectedIdIluminacao = radioIluminacao.getCheckedRadioButtonId();
        int selectedIdSolo = radioTipoSolo.getCheckedRadioButtonId();

        String TipoPosteSelecionado = spnTiposPostes.getSelectedItem().toString();
        String CapacidadeSelecionado = spnCapacidade.getSelectedItem().toString();

        Intent intent = new Intent(this, CadastrarExtruturas.class);
        intent.putExtra("USERTELA", "1");
        intent.putExtra("numeroPlaca", numeroPlaca);
        intent.putExtra("telefonia1", String.valueOf(radioTelefoniaTL.isChecked()));
        intent.putExtra("telefonia3", String.valueOf(radioTelefoniaNET.isChecked()));
        intent.putExtra("telefonia4", String.valueOf(radioTelefoniaFO.isChecked()));
        intent.putExtra("vidautil", String.valueOf(radioVidaUtil.isChecked()));
        intent.putExtra("estai", estai);
        intent.putExtra("selectedIdAltura", selectedStringAltura);
        intent.putExtra("selectedIdIluminacao", String.valueOf(selectedIdIluminacao));
        intent.putExtra("selectedIdAcesso", selectedIdAcesso);
        intent.putExtra("TipoPosteSelecionado", TipoPosteSelecionado);
        intent.putExtra("CapacidadeSelecionado", CapacidadeSelecionado);
        intent.putExtra("TipoSoloSelecionado", String.valueOf(selectedIdSolo));
        if (valorPagina != null && valorPagina.equals("1")) {
            intent.putExtra("edit", "1");
        }

        this.startActivity(intent);
        finish();
    }

    public void irProximoConsEquipamento(View v) {
        salvaDadosBancos();
    }

    private void salvaDadosBancos() {
        String iluminacao = "";
        String telefonia = "";
        String finalVidaUtil = "";
        String solo = "";
        String TipoPosteSelecionado = "";
        String CapacidadeSelecionado = "";

        String numeroPlaca = textNumeroPlaca.getText().toString();
        numeroPlaca = numeroPlaca.toUpperCase();

        String estai = textEstai.getText().toString();
        estai = estai.toUpperCase();

        int selectedIdIluminacao = radioIluminacao.getCheckedRadioButtonId();
        int selectedIdSolo = radioTipoSolo.getCheckedRadioButtonId();


        if (spnTiposPostes.getSelectedItem().toString().equals("Selecione um Tipo")) {
            TipoPosteSelecionado = "";
        } else {
            TipoPosteSelecionado = spnTiposPostes.getSelectedItem().toString();
        }

        if (spnCapacidade.getSelectedItem().toString().equals("Selecione a capacidade")) {
            CapacidadeSelecionado = "";
        } else {
            CapacidadeSelecionado = spnCapacidade.getSelectedItem().toString();
        }

        if (selectedIdSolo == btA.getId()) {
            solo = "A";
        } else if (selectedIdSolo == btB.getId()) {
            solo = "B";
        } else if (selectedIdSolo == btC.getId()) {
            solo = "C";
        }

        if (selectedIdIluminacao == btSim.getId()) {
            iluminacao = "Sim";
        } else if (selectedIdIluminacao == btNao.getId()) {
            iluminacao = "Não";
        }

        if (radioTelefoniaTL.isChecked()) {
            if (telefonia.equals("")) {
                telefonia = "TL";
            } else {
                telefonia = telefonia + "-TL";
            }
        }

        if (radioTelefoniaNET.isChecked()) {
            if (telefonia.equals("")) {
                telefonia = "NET";
            } else {
                telefonia = telefonia + "-NET";
            }
        }
        if (radioTelefoniaFO.isChecked()) {
            if (telefonia.equals("")) {
                telefonia = "FO";
            } else {
                telefonia = telefonia + "-FO";
            }
        }

        if (radioVidaUtil.isChecked()) {
            finalVidaUtil = "Sim";
        } else {
            finalVidaUtil = "-";
        }

        if (TipoPosteSelecionado.equals("") || selectedStringAltura.equals(" ") || CapacidadeSelecionado.equals("") || iluminacao.equals("") || selectedIdAcesso.equals("") || solo.equals("")) {
            Toast.makeText(getApplicationContext(), "Por Favor! Insira os dados obrigatórios!!", Toast.LENGTH_SHORT).show();
        } else {
            db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
            ContentValues values = new ContentValues();
            values.put("TIPOPOSTE", TipoPosteSelecionado);
            values.put("ALTURA", selectedStringAltura);
            values.put("CAPACIDADE", CapacidadeSelecionado);
            values.put("NUMPLACA", numeroPlaca);
            values.put("ILUMICACAOPUBLICA", iluminacao);
            values.put("TELEFONIA", telefonia);
            values.put("QUANTIDADE", estai);
            values.put("ACESSO", selectedIdAcesso);
            values.put("TIPOSOLO", solo);
            values.put("FINALVIDAUTIL", finalVidaUtil);
            db.update(TABELAPOSTE, values, "ID=" + idPoste, null);
            db.close();

            Intent intent = new Intent(this, CadastrarEquipamento.class);
            if (valorPagina != null) {
                intent.putExtra("USERTELA", "EDITAR");
            }
            this.startActivity(intent);
            finish();
        }
    }

    public void bt8(View v) {
        selectedStringAltura = "8";

        bt9.setChecked(false);
        bt10.setChecked(false);
        bt11.setChecked(false);
        bt12.setChecked(false);
        bt13.setChecked(false);
        bt14.setChecked(false);
        bt15.setChecked(false);
        bt16.setChecked(false);
        bt17.setChecked(false);
        bt18.setChecked(false);

        bt8.setTextColor(Color.RED);
        bt9.setTextColor(Color.BLACK);
        bt10.setTextColor(Color.BLACK);
        bt11.setTextColor(Color.BLACK);
        bt12.setTextColor(Color.BLACK);
        bt13.setTextColor(Color.BLACK);
        bt14.setTextColor(Color.BLACK);
        bt15.setTextColor(Color.BLACK);
        bt16.setTextColor(Color.BLACK);
        bt17.setTextColor(Color.BLACK);
        bt18.setTextColor(Color.BLACK);
    }

    public void bt9(View v) {
        selectedStringAltura = "9";

        bt8.setChecked(false);
        bt10.setChecked(false);
        bt11.setChecked(false);
        bt12.setChecked(false);
        bt13.setChecked(false);
        bt14.setChecked(false);
        bt15.setChecked(false);
        bt16.setChecked(false);
        bt17.setChecked(false);
        bt18.setChecked(false);

        bt9.setTextColor(Color.RED);
        bt16.setTextColor(Color.BLACK);
        bt10.setTextColor(Color.BLACK);
        bt11.setTextColor(Color.BLACK);
        bt12.setTextColor(Color.BLACK);
        bt13.setTextColor(Color.BLACK);
        bt14.setTextColor(Color.BLACK);
        bt15.setTextColor(Color.BLACK);
        bt8.setTextColor(Color.BLACK);
        bt17.setTextColor(Color.BLACK);
        bt18.setTextColor(Color.BLACK);
    }

    public void bt10(View v) {
        selectedStringAltura = "10";

        bt9.setChecked(false);
        bt8.setChecked(false);
        bt11.setChecked(false);
        bt12.setChecked(false);
        bt13.setChecked(false);
        bt14.setChecked(false);
        bt15.setChecked(false);
        bt16.setChecked(false);
        bt17.setChecked(false);
        bt18.setChecked(false);

        bt10.setTextColor(Color.RED);
        bt9.setTextColor(Color.BLACK);
        bt16.setTextColor(Color.BLACK);
        bt11.setTextColor(Color.BLACK);
        bt12.setTextColor(Color.BLACK);
        bt13.setTextColor(Color.BLACK);
        bt14.setTextColor(Color.BLACK);
        bt15.setTextColor(Color.BLACK);
        bt8.setTextColor(Color.BLACK);
        bt17.setTextColor(Color.BLACK);
        bt18.setTextColor(Color.BLACK);
    }

    public void bt11(View v) {
        selectedStringAltura = "11";

        bt9.setChecked(false);
        bt10.setChecked(false);
        bt8.setChecked(false);
        bt12.setChecked(false);
        bt13.setChecked(false);
        bt14.setChecked(false);
        bt15.setChecked(false);
        bt16.setChecked(false);
        bt17.setChecked(false);
        bt18.setChecked(false);

        bt11.setTextColor(Color.RED);
        bt9.setTextColor(Color.BLACK);
        bt10.setTextColor(Color.BLACK);
        bt16.setTextColor(Color.BLACK);
        bt12.setTextColor(Color.BLACK);
        bt13.setTextColor(Color.BLACK);
        bt14.setTextColor(Color.BLACK);
        bt15.setTextColor(Color.BLACK);
        bt8.setTextColor(Color.BLACK);
        bt17.setTextColor(Color.BLACK);
        bt18.setTextColor(Color.BLACK);
    }

    public void bt12(View v) {
        selectedStringAltura = "12";

        bt9.setChecked(false);
        bt10.setChecked(false);
        bt11.setChecked(false);
        bt8.setChecked(false);
        bt13.setChecked(false);
        bt14.setChecked(false);
        bt15.setChecked(false);
        bt16.setChecked(false);
        bt17.setChecked(false);
        bt18.setChecked(false);

        bt12.setTextColor(Color.RED);
        bt9.setTextColor(Color.BLACK);
        bt10.setTextColor(Color.BLACK);
        bt11.setTextColor(Color.BLACK);
        bt16.setTextColor(Color.BLACK);
        bt13.setTextColor(Color.BLACK);
        bt14.setTextColor(Color.BLACK);
        bt15.setTextColor(Color.BLACK);
        bt8.setTextColor(Color.BLACK);
        bt17.setTextColor(Color.BLACK);
        bt18.setTextColor(Color.BLACK);
    }

    public void bt13(View v) {
        selectedStringAltura = "13";

        bt9.setChecked(false);
        bt10.setChecked(false);
        bt11.setChecked(false);
        bt12.setChecked(false);
        bt8.setChecked(false);
        bt14.setChecked(false);
        bt15.setChecked(false);
        bt16.setChecked(false);
        bt17.setChecked(false);
        bt18.setChecked(false);

        bt13.setTextColor(Color.RED);
        bt9.setTextColor(Color.BLACK);
        bt10.setTextColor(Color.BLACK);
        bt11.setTextColor(Color.BLACK);
        bt12.setTextColor(Color.BLACK);
        bt16.setTextColor(Color.BLACK);
        bt14.setTextColor(Color.BLACK);
        bt15.setTextColor(Color.BLACK);
        bt8.setTextColor(Color.BLACK);
        bt17.setTextColor(Color.BLACK);
        bt18.setTextColor(Color.BLACK);
    }

    public void bt14(View v) {
        selectedStringAltura = "14";

        bt9.setChecked(false);
        bt10.setChecked(false);
        bt11.setChecked(false);
        bt12.setChecked(false);
        bt13.setChecked(false);
        bt8.setChecked(false);
        bt15.setChecked(false);
        bt16.setChecked(false);
        bt17.setChecked(false);
        bt18.setChecked(false);

        bt14.setTextColor(Color.RED);
        bt9.setTextColor(Color.BLACK);
        bt10.setTextColor(Color.BLACK);
        bt11.setTextColor(Color.BLACK);
        bt12.setTextColor(Color.BLACK);
        bt13.setTextColor(Color.BLACK);
        bt16.setTextColor(Color.BLACK);
        bt15.setTextColor(Color.BLACK);
        bt8.setTextColor(Color.BLACK);
        bt17.setTextColor(Color.BLACK);
        bt18.setTextColor(Color.BLACK);
    }

    public void bt15(View v) {
        selectedStringAltura = "15";

        bt9.setChecked(false);
        bt10.setChecked(false);
        bt11.setChecked(false);
        bt12.setChecked(false);
        bt13.setChecked(false);
        bt14.setChecked(false);
        bt8.setChecked(false);
        bt16.setChecked(false);
        bt17.setChecked(false);
        bt18.setChecked(false);

        bt15.setTextColor(Color.RED);
        bt9.setTextColor(Color.BLACK);
        bt10.setTextColor(Color.BLACK);
        bt11.setTextColor(Color.BLACK);
        bt12.setTextColor(Color.BLACK);
        bt13.setTextColor(Color.BLACK);
        bt14.setTextColor(Color.BLACK);
        bt16.setTextColor(Color.BLACK);
        bt8.setTextColor(Color.BLACK);
        bt17.setTextColor(Color.BLACK);
        bt18.setTextColor(Color.BLACK);
    }

    public void bt16(View v) {
        selectedStringAltura = "16";

        bt9.setChecked(false);
        bt10.setChecked(false);
        bt11.setChecked(false);
        bt12.setChecked(false);
        bt13.setChecked(false);
        bt14.setChecked(false);
        bt15.setChecked(false);
        bt8.setChecked(false);
        bt17.setChecked(false);
        bt18.setChecked(false);

        bt16.setTextColor(Color.RED);
        bt9.setTextColor(Color.BLACK);
        bt10.setTextColor(Color.BLACK);
        bt11.setTextColor(Color.BLACK);
        bt12.setTextColor(Color.BLACK);
        bt13.setTextColor(Color.BLACK);
        bt14.setTextColor(Color.BLACK);
        bt15.setTextColor(Color.BLACK);
        bt8.setTextColor(Color.BLACK);
        bt17.setTextColor(Color.BLACK);
        bt18.setTextColor(Color.BLACK);
    }

    public void bt17(View v) {
        selectedStringAltura = "17";

        bt9.setChecked(false);
        bt10.setChecked(false);
        bt11.setChecked(false);
        bt12.setChecked(false);
        bt13.setChecked(false);
        bt14.setChecked(false);
        bt15.setChecked(false);
        bt16.setChecked(false);
        bt8.setChecked(false);
        bt18.setChecked(false);

        bt17.setTextColor(Color.RED);
        bt9.setTextColor(Color.BLACK);
        bt10.setTextColor(Color.BLACK);
        bt11.setTextColor(Color.BLACK);
        bt12.setTextColor(Color.BLACK);
        bt13.setTextColor(Color.BLACK);
        bt14.setTextColor(Color.BLACK);
        bt15.setTextColor(Color.BLACK);
        bt8.setTextColor(Color.BLACK);
        bt16.setTextColor(Color.BLACK);
        bt18.setTextColor(Color.BLACK);
    }

    public void bt18(View v) {
        selectedStringAltura = "18";

        bt9.setChecked(false);
        bt10.setChecked(false);
        bt11.setChecked(false);
        bt12.setChecked(false);
        bt13.setChecked(false);
        bt14.setChecked(false);
        bt15.setChecked(false);
        bt16.setChecked(false);
        bt17.setChecked(false);
        bt8.setChecked(false);

        bt18.setTextColor(Color.RED);
        bt9.setTextColor(Color.BLACK);
        bt10.setTextColor(Color.BLACK);
        bt11.setTextColor(Color.BLACK);
        bt12.setTextColor(Color.BLACK);
        bt13.setTextColor(Color.BLACK);
        bt14.setTextColor(Color.BLACK);
        bt15.setTextColor(Color.BLACK);
        bt8.setTextColor(Color.BLACK);
        bt17.setTextColor(Color.BLACK);
        bt16.setTextColor(Color.BLACK);
    }

    public void btAcesso1(View v) {
        selectedIdAcesso = "1";
        btAcesso2.setChecked(false);
        btAcesso3.setChecked(false);
        btAcesso4.setChecked(false);
        btAcesso5.setChecked(false);
        btAcesso6.setChecked(false);
        btAcesso7.setChecked(false);
        btAcesso8.setChecked(false);
    }

    public void btAcesso2(View v) {
        selectedIdAcesso = "2";
        btAcesso1.setChecked(false);
        btAcesso3.setChecked(false);
        btAcesso4.setChecked(false);
        btAcesso5.setChecked(false);
        btAcesso6.setChecked(false);
        btAcesso7.setChecked(false);
        btAcesso8.setChecked(false);
    }

    public void btAcesso3(View v) {
        selectedIdAcesso = "3";
        btAcesso2.setChecked(false);
        btAcesso1.setChecked(false);
        btAcesso4.setChecked(false);
        btAcesso5.setChecked(false);
        btAcesso6.setChecked(false);
        btAcesso7.setChecked(false);
        btAcesso8.setChecked(false);
    }

    public void btAcesso4(View v) {
        selectedIdAcesso = "4";
        btAcesso2.setChecked(false);
        btAcesso3.setChecked(false);
        btAcesso1.setChecked(false);
        btAcesso5.setChecked(false);
        btAcesso6.setChecked(false);
        btAcesso7.setChecked(false);
        btAcesso8.setChecked(false);
    }

    public void btAcesso5(View v) {
        selectedIdAcesso = "5";
        btAcesso2.setChecked(false);
        btAcesso3.setChecked(false);
        btAcesso4.setChecked(false);
        btAcesso1.setChecked(false);
        btAcesso6.setChecked(false);
        btAcesso7.setChecked(false);
        btAcesso8.setChecked(false);
    }

    public void btAcesso6(View v) {
        selectedIdAcesso = "6";
        btAcesso2.setChecked(false);
        btAcesso3.setChecked(false);
        btAcesso4.setChecked(false);
        btAcesso5.setChecked(false);
        btAcesso1.setChecked(false);
        btAcesso7.setChecked(false);
        btAcesso8.setChecked(false);
    }

    public void btAcesso7(View v) {
        selectedIdAcesso = "7";
        btAcesso2.setChecked(false);
        btAcesso3.setChecked(false);
        btAcesso4.setChecked(false);
        btAcesso5.setChecked(false);
        btAcesso6.setChecked(false);
        btAcesso1.setChecked(false);
        btAcesso8.setChecked(false);
    }

    public void btAcesso8(View v) {
        selectedIdAcesso = "8";
        btAcesso2.setChecked(false);
        btAcesso3.setChecked(false);
        btAcesso4.setChecked(false);
        btAcesso5.setChecked(false);
        btAcesso6.setChecked(false);
        btAcesso7.setChecked(false);
        btAcesso1.setChecked(false);
    }

    private void inflaListaEstruturasComIdLocacao() {
        ArrayList<DadosGerais> itensDoc = new ArrayList<DadosGerais>();

        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        Cursor linhas = db.rawQuery("SELECT * FROM " + TABELAESTRUTURA + " WHERE IDPOSTE = " + idPoste + ";", null);
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

        adapterListViewConsumidores = new AdapterListViewConsumidores(this, itensDoc);
        listViewEstruturas.setAdapter(adapterListViewConsumidores);
        listViewEstruturas.setCacheColorHint(Color.TRANSPARENT);

        linhas.close();
        Helper.getListViewSize(listViewEstruturas);
        db.close();
    }

    public void deletaItem(View v) {
        adapterListViewConsumidores.removeItem((Integer) v.getTag());
        adapterListViewConsumidores.notifyDataSetChanged();
        String idMensagem = adapterListViewConsumidores.idSelecionado;
        confirmarDelete(idMensagem);
    }

    public void editaItem(View v) {
        adapterListViewConsumidores.editaItem((Integer) v.getTag());
        String idMensagem = adapterListViewConsumidores.idSelecionado;


        String numeroPlaca = textNumeroPlaca.getText().toString();
        String estai = textEstai.getText().toString();

        numeroPlaca = numeroPlaca.toUpperCase();
        estai = estai.toUpperCase();

        int selectedIdAltura = radioAltura.getCheckedRadioButtonId();
        int selectedIdIluminacao = radioIluminacao.getCheckedRadioButtonId();
        int selectedIdSolo = radioTipoSolo.getCheckedRadioButtonId();

        String TipoPosteSelecionado = spnTiposPostes.getSelectedItem().toString();
        String CapacidadeSelecionado = spnCapacidade.getSelectedItem().toString();

        Intent intent = new Intent(this, CadastrarExtruturas.class);
        intent.putExtra("USERTELA", "EDITAR");
        intent.putExtra("numeroPlaca", numeroPlaca);
        intent.putExtra("telefonia1", String.valueOf(radioTelefoniaTL.isChecked()));
        intent.putExtra("telefonia3", String.valueOf(radioTelefoniaNET.isChecked()));
        intent.putExtra("telefonia4", String.valueOf(radioTelefoniaFO.isChecked()));
        intent.putExtra("vidautil", String.valueOf(radioVidaUtil.isChecked()));
        intent.putExtra("estai", estai);
        intent.putExtra("selectedIdAltura", selectedStringAltura);
        intent.putExtra("selectedIdIluminacao", String.valueOf(selectedIdIluminacao));
        intent.putExtra("selectedIdAcesso", selectedIdAcesso);
        intent.putExtra("TipoPosteSelecionado", TipoPosteSelecionado);
        intent.putExtra("CapacidadeSelecionado", CapacidadeSelecionado);
        intent.putExtra("TipoSoloSelecionado", String.valueOf(selectedIdSolo));
        intent.putExtra("id", idMensagem);
        if (valorPagina != null && valorPagina.equals("1")) {
            intent.putExtra("edit", "1");
        }

        this.startActivity(intent);
        finish();
    }

    private void confirmarDelete(final String idMensagem) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Tem certeza que deseja deletar esta Estrutura?");

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

    private void deletarMensagem(final String idMens) {
        int idExcluido = Integer.parseInt(idMens.toString());
        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        db.execSQL("DELETE FROM " + TABELAESTRUTURA + " WHERE ID = " + idExcluido + "");
        db.close();

        inflaListaEstruturasComIdLocacao();
    }

    public void preencheDadosEdicao() {
        String tipo = null;
        String altura = null;
        String capacidade = null;
        String placa = null;
        String iluminacao = null;
        String telefonia = null;
        String estai = null;
        String acesso = null;
        String tipoSolo = null;
        String vidaUT = null;

        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        Cursor linhas = db.rawQuery("SELECT * FROM " + TABELAPOSTE + " WHERE ID = " + idPoste + ";", null);
        if (linhas.moveToFirst()) {
            do {
                tipo = linhas.getString(1);
                altura = linhas.getString(2);
                capacidade = linhas.getString(3);
                placa = linhas.getString(4);
                iluminacao = linhas.getString(5);
                telefonia = linhas.getString(6);
                estai = linhas.getString(7);
                acesso = linhas.getString(8);
                tipoSolo = linhas.getString(9);
                vidaUT = linhas.getString(10);

            }
            while (linhas.moveToNext());
            linhas.close();
        }
        db.close();
        if (acesso != null) {
            if (acesso.equals("1")) {
                selectedIdAcesso = "1";
                radioAcesso.check(R.id.btAcesso1);
            } else if (acesso.equals("2")) {
                selectedIdAcesso = "2";
                radioAcesso.check(R.id.btAcesso2);
            } else if (acesso.equals("3")) {
                selectedIdAcesso = "3";
                radioAcesso.check(R.id.btAcesso3);
            } else if (acesso.equals("4")) {
                selectedIdAcesso = "4";
                radioAcesso.check(R.id.btAcesso4);
            } else if (acesso.equals("5")) {
                selectedIdAcesso = "5";
                radioAcesso.check(R.id.btAcesso5);
            } else if (acesso.equals("6")) {
                selectedIdAcesso = "6";
                radioAcesso.check(R.id.btAcesso6);
            } else if (acesso.equals("7")) {
                selectedIdAcesso = "7";
                radioAcesso.check(R.id.btAcesso7);
            } else if (acesso.equals("8")) {
                selectedIdAcesso = "8";
                radioAcesso.check(R.id.btAcesso8);
            }
        }

        if (altura != null) {
            if (altura.equals("8")) {
                selectedStringAltura = "8";
                bt8.setChecked(true);
                bt8.setTextColor(Color.RED);

            } else if (altura.equals("9")) {
                selectedStringAltura = "9";
                bt9.setChecked(true);
                bt9.setTextColor(Color.RED);

            } else if (altura.equals("10")) {
                selectedStringAltura = "10";
                bt10.setChecked(true);
                bt10.setTextColor(Color.RED);

            } else if (altura.equals("11")) {
                selectedStringAltura = "11";
                bt11.setChecked(true);
                bt11.setTextColor(Color.RED);

            } else if (altura.equals("12")) {
                selectedStringAltura = "12";
                bt12.setChecked(true);
                bt12.setTextColor(Color.RED);

            } else if (altura.equals("13")) {
                selectedStringAltura = "13";
                bt13.setChecked(true);
                bt13.setTextColor(Color.RED);

            } else if (altura.equals("14")) {
                selectedStringAltura = "14";
                bt14.setChecked(true);
                bt14.setTextColor(Color.RED);

            } else if (altura.equals("15")) {
                selectedStringAltura = "15";
                bt15.setChecked(true);
                bt15.setTextColor(Color.RED);

            } else if (altura.equals("16")) {
                selectedStringAltura = "16";
                bt16.setChecked(true);
                bt16.setTextColor(Color.RED);

            } else if (altura.equals("17")) {
                selectedStringAltura = "17";
                bt17.setChecked(true);
                bt17.setTextColor(Color.RED);

            } else if (altura.equals("18")) {
                selectedStringAltura = "18";
                bt18.setChecked(true);
                bt18.setTextColor(Color.RED);
            }
        }

        if (iluminacao != null) {
            if (iluminacao.equals("Sim")) {
                radioIluminacao.check(R.id.btSim);
            } else if (iluminacao.equals("Não")) {
                radioIluminacao.check(R.id.btNao);
            }
        }

        if (tipoSolo != null) {
            if (tipoSolo.equals("A")) {
                radioTipoSolo.check(R.id.btA);
            } else if (tipoSolo.equals("B")) {
                radioTipoSolo.check(R.id.btB);
            } else if (tipoSolo.equals("C")) {
                radioTipoSolo.check(R.id.btC);
            }
        }
        if (estai != null) {
            textEstai.setText(estai);
        }

        if (placa != null) {
            textNumeroPlaca.setText(placa);
        }

        if (tipo != null) {
            int spinnerPosition = adapter.getPosition(tipo);
            spnTiposPostes.setSelection(spinnerPosition);
        }

        if (capacidade != null) {
            int spinnerPosition2 = adapter2.getPosition(capacidade);
            spnCapacidade.setSelection(spinnerPosition2);
        }
        if (telefonia != null) {
            String[] textoSeparado = telefonia.split("-");

            for (int i = 0; i < textoSeparado.length; i++) {

                if (textoSeparado[i].equals("TL")) {
                    radioTelefoniaTL.setChecked(true);
                }
                if (textoSeparado[i].equals("NET")) {
                    radioTelefoniaNET.setChecked(true);
                }
                if (textoSeparado[i].equals("FO")) {
                    radioTelefoniaFO.setChecked(true);
                }
            }
        }
        if (vidaUT != null) {
            if (vidaUT.equals("Sim")) {
                radioVidaUtil.setChecked(true);
            }
        }
    }
}
