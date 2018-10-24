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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class CadastrarConsumidores extends AppCompatActivity {

    private RadioGroup grupLado;
    private RadioButton radioDireita, radioEsquerda;
    private Spinner spnD10, spnQ10, spnW01, spnS03, spnQ25, spnW02, spnAF, spnQ35, spnW031, spnW032, spnW033;
    ArrayList<String> spn1 = new ArrayList<String>();

    SQLiteDatabase db;
    String BANCO = "banco.db";
    String TABELACONSUMIDOR = "consumidor";
    private String vaiEditar = "0", veioEdicao = "0";
    ArrayAdapter adapter;
    private String tipoEquipamento, numeroPlacaEquipamento, tensao, descricao, idConsumidor, idLocacao, idPoste;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adiciona_consumidores);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Aplicativo HCC");
        toolbar.setSubtitle("Dados do Consumidores");

        //Supote ao Toobar(Apresenta Icone voltar, na barra de cima da tela)
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        grupLado = (RadioGroup) findViewById(R.id.grupLado);
        radioDireita = (RadioButton) findViewById(R.id.radioDireita);
        radioEsquerda = (RadioButton) findViewById(R.id.radioEsquerda);

        spnD10 = (Spinner) findViewById(R.id.spnD10);
        spnQ10 = (Spinner) findViewById(R.id.spnQ10);
        spnW01 = (Spinner) findViewById(R.id.spnW01);
        spnS03 = (Spinner) findViewById(R.id.spnS03);
        spnQ25 = (Spinner) findViewById(R.id.spnQ25);
        spnW02 = (Spinner) findViewById(R.id.spnW02);
        spnAF = (Spinner) findViewById(R.id.spnAF);
        spnQ35 = (Spinner) findViewById(R.id.spnQ35);
        spnW031 = (Spinner) findViewById(R.id.spnW031);
        spnW032 = (Spinner) findViewById(R.id.spnW032);
        spnW033 = (Spinner) findViewById(R.id.spnW033);

        spn1.add("0");
        spn1.add("1");
        spn1.add("2");
        spn1.add("3");
        spn1.add("4");
        spn1.add("5");
        spn1.add("6");
        spn1.add("7");
        spn1.add("8");
        spn1.add("9");
        spn1.add("10");

        adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, spn1);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnD10.setAdapter(adapter);
        spnQ10.setAdapter(adapter);
        spnW01.setAdapter(adapter);
        spnS03.setAdapter(adapter);
        spnQ25.setAdapter(adapter);
        spnW02.setAdapter(adapter);
        spnAF.setAdapter(adapter);
        spnQ35.setAdapter(adapter);
        spnW031.setAdapter(adapter);
        spnW032.setAdapter(adapter);
        spnW033.setAdapter(adapter);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        // Verifica se existe dados enviados da pagina anterior
        if (getIntent().getStringExtra("USERTELA") != null) {
            tipoEquipamento = bundle.getString("tipo");
            numeroPlacaEquipamento = bundle.getString("placa");
            tensao = bundle.getString("tensao");
            descricao = bundle.getString("descricao");

            // Verifica se veio para edição, se sim: variavel Vaieditar recebe 1 e é pego o id do Consumidor que será editado
            if (getIntent().getStringExtra("USERTELA").equals("EDITAR")) {
                vaiEditar = "1";
                idConsumidor = bundle.getString("id");
                preencheDadosEdicaoConsumidores(idConsumidor);
            }

            // Verifica se a pagina anterior estava sendo editada ou inserida uma nova
            // Se sim, variavel veiEdicao recebe 1 (pois qunado retornar a pagina anterior, avisar o sistema que aquela pagina era uma edição)
            if (getIntent().getStringExtra("edit") != null && getIntent().getStringExtra("edit").equals("1")) {
                veioEdicao = "1";
            }
        }
    }

    //Metodo que pega ação do botçao voltar, no toolbar bem em cima da tela
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
                                if (veioEdicao.equals("1")) { // se sim, manda um put extra avisando para continuar a edição
                                    Intent intent = new Intent(CadastrarConsumidores.this, CadastrarEquipamento.class);
                                    intent.putExtra("USERTELA", "EDITAR");
                                    CadastrarConsumidores.this.startActivity(intent);
                                    finish();
                                } else { // se não, envia os dados que estavam sendo inseridos(Caso haja)
                                    Intent intent = new Intent(CadastrarConsumidores.this, CadastrarEquipamento.class);
                                    intent.putExtra("USERTELA", "10");
                                    intent.putExtra("tipo", tipoEquipamento);
                                    intent.putExtra("placa", numeroPlacaEquipamento);
                                    intent.putExtra("descricao", descricao);
                                    intent.putExtra("tensao", tensao);
                                    CadastrarConsumidores.this.startActivity(intent);
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
            default:
                break;
        }
        return true;
    }

    //Metodo responsavel em buscar no banco os dados do consumidor selecionado inseri-los nos seus devidos campos.
    public void preencheDadosEdicaoConsumidores(String idConsumidor) {
        String lado = null;
        String ramal = null;

        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);

        //select que busca os dados na tabela Consumidor, utilizando ID do consumidor selecionado
        Cursor linhas = db.rawQuery("SELECT * FROM " + TABELACONSUMIDOR + " WHERE ID = " + idConsumidor + ";", null);
        if (linhas.moveToFirst()) {
            do {
                //Pega o lado(Direito ou Esquerdo) e o ramal do respectivo consumidor
                lado = linhas.getString(1);
                ramal = linhas.getString(4);
            }
            while (linhas.moveToNext());
            linhas.close();
        }
        db.close();

        //Verifica se o Lado não é nulo
        //Caso não seja nulo, verifica se é direito(D) ou esquerdo(E), deixando clicado seu respectivo radioButton
        if (lado != null) {
            if (lado.equals("E")) {
                grupLado.check(R.id.radioEsquerda);
            } else if (lado.equals("D")) {
                grupLado.check(R.id.radioDireita);
            }
        }

        //Verifica se o Lado não é nulo
        if (ramal != null) {

            // caso nãos seja nulo, separa o Ramal em uma lista
            //Pois o ramal pode ser de 1 a 10 tipos
            String[] textoSeparado1 = ramal.split(" ");

            //Para cada ramal verifica-se de qual tipo é(D10,S03,Af, etc), após pega a qunatidade e inseri no seu respectivo spinner
            for (int i = 0; i < textoSeparado1.length; i++) {
                StringTokenizer st = new StringTokenizer(textoSeparado1[i]);
                String idLocoPrev = st.nextToken(":");
                String idNext = st.nextToken(":");

                if (idLocoPrev.equals("D10")) {
                    int spinnerPosition = adapter.getPosition(idNext);
                    spnD10.setSelection(spinnerPosition);
                }
                if (idLocoPrev.equals("S03")) {
                    int spinnerPosition = adapter.getPosition(idNext);
                    spnS03.setSelection(spinnerPosition);
                }
                if (idLocoPrev.equals("AF")) {
                    int spinnerPosition = adapter.getPosition(idNext);
                    spnAF.setSelection(spinnerPosition);
                }
                if (idLocoPrev.equals("Q10")) {
                    int spinnerPosition = adapter.getPosition(idNext);
                    spnQ10.setSelection(spinnerPosition);
                }
                if (idLocoPrev.equals("Q25")) {
                    int spinnerPosition = adapter.getPosition(idNext);
                    spnQ25.setSelection(spinnerPosition);
                }
                if (idLocoPrev.equals("Q35")) {
                    int spinnerPosition = adapter.getPosition(idNext);
                    spnQ35.setSelection(spinnerPosition);
                }
                if (idLocoPrev.equals("W01")) {
                    int spinnerPosition = adapter.getPosition(idNext);
                    spnW01.setSelection(spinnerPosition);
                }
                if (idLocoPrev.equals("W02")) {
                    int spinnerPosition = adapter.getPosition(idNext);
                    spnW02.setSelection(spinnerPosition);
                }
                if (idLocoPrev.equals("W03(10mm)")) {
                    int spinnerPosition = adapter.getPosition(idNext);
                    spnW031.setSelection(spinnerPosition);
                }
                if (idLocoPrev.equals("W03(16mm)")) {
                    int spinnerPosition = adapter.getPosition(idNext);
                    spnW032.setSelection(spinnerPosition);
                }
                if (idLocoPrev.equals("W03(25mm)")) {
                    int spinnerPosition = adapter.getPosition(idNext);
                    spnW033.setSelection(spinnerPosition);
                }
            }
        }
    }

    //Pega o clique do botão e chama o metodo de inserção no banco
    public void salvaConsumidor(View v) {
        salvaDadosConsumidor();
    }

    //Metodo que salva os dados no banco
    private void salvaDadosConsumidor() {
        SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
        idPoste = sharedpreferences.getString("idPosteKey", null);
        idLocacao = sharedpreferences.getString("idLocacaoKey", null);

        String lado = " ";
        String RamalCompleto = "";

        //Verifica quais dos Ramais foram inseridoa quantidade
        //E inseri todos na variavel RamalComleto
        if (!spnD10.getSelectedItem().toString().equals("0")) {
            RamalCompleto = RamalCompleto + "D10:" + spnD10.getSelectedItem().toString() + " ";
        }
        if (!spnS03.getSelectedItem().toString().equals("0")) {
            RamalCompleto = RamalCompleto + "S03:" + spnS03.getSelectedItem().toString() + " ";
        }
        if (!spnAF.getSelectedItem().toString().equals("0")) {
            RamalCompleto = RamalCompleto + "AF:" + spnAF.getSelectedItem().toString() + " ";
        }
        if (!spnQ10.getSelectedItem().toString().equals("0")) {
            RamalCompleto = RamalCompleto + "Q10:" + spnQ10.getSelectedItem().toString() + " ";
        }
        if (!spnQ25.getSelectedItem().toString().equals("0")) {
            RamalCompleto = RamalCompleto + "Q25:" + spnQ25.getSelectedItem().toString() + " ";
        }
        if (!spnQ35.getSelectedItem().toString().equals("0")) {
            RamalCompleto = RamalCompleto + "Q35:" + spnQ35.getSelectedItem().toString() + " ";
        }
        if (!spnW01.getSelectedItem().toString().equals("0")) {
            RamalCompleto = RamalCompleto + "W01:" + spnW01.getSelectedItem().toString() + " ";
        }
        if (!spnW02.getSelectedItem().toString().equals("0")) {
            RamalCompleto = RamalCompleto + "W02:" + spnW02.getSelectedItem().toString() + " ";
        }
        if (!spnW031.getSelectedItem().toString().equals("0")) {
            RamalCompleto = RamalCompleto + "W03(10mm):" + spnW031.getSelectedItem().toString() + " ";
        }
        if (!spnW032.getSelectedItem().toString().equals("0")) {
            RamalCompleto = RamalCompleto + "W03(16mm):" + spnW032.getSelectedItem().toString() + " ";
        }
        if (!spnW033.getSelectedItem().toString().equals("0")) {
            RamalCompleto = RamalCompleto + "W03(25mm):" + spnW033.getSelectedItem().toString() + " ";
        }

        //Verifica qual radio tá clicado, e coloca na variavel lado
        int selectedIdLado = grupLado.getCheckedRadioButtonId();
        if (selectedIdLado == radioDireita.getId()) {
            lado = "D";
        } else if (selectedIdLado == radioEsquerda.getId()) {
            lado = "E";
        }

        //Verifica os itens obrigatórios
        if (lado.equals(" ") || RamalCompleto.equals("")) {
            Toast.makeText(getApplicationContext(), "Por Favor! Insira os dados obrigatórios!!", Toast.LENGTH_SHORT).show();
        } else {

            //Verifica se é uma edição
            if (vaiEditar.equals("1")) { //se for, faz update
                db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
                ContentValues values = new ContentValues();
                values.put("LADO", lado);
                values.put("RAMAL", RamalCompleto);
                db.update(TABELACONSUMIDOR, values, "ID=" + idConsumidor, null);
                db.close();
            } else { //se não for, faz um insert
                db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
                ContentValues values = new ContentValues();
                values.put("LADO", lado);
                values.put("RAMAL", RamalCompleto);
                values.put("IDPOSTE", idPoste);

                db.insert(TABELACONSUMIDOR, null, values);
                db.close();
            }

            //Verifica se a pagina anterior estava sendo editada
            if (veioEdicao.equals("1")) { // se sim, manda um put extra avisando para continuar a edição
                Intent intent = new Intent(this, CadastrarEquipamento.class);
                intent.putExtra("USERTELA", "EDITAR");
                this.startActivity(intent);
                finish();
            } else { // se não, envia os dados que estavam sendo inseridos(Caso haja)
                Intent intent = new Intent(this, CadastrarEquipamento.class);
                intent.putExtra("USERTELA", "10");
                intent.putExtra("tipo", tipoEquipamento);
                intent.putExtra("placa", numeroPlacaEquipamento);
                intent.putExtra("descricao", descricao);
                intent.putExtra("tensao", tensao);
                this.startActivity(intent);
                finish();
            }
        }
    }

    //Metodo responsavel por pegar ação do botão nativo "Voltar" do smartfone
    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Tem certeza que deseja sair desta aba? Os dados ainda não foram salvos");
        alertDialogBuilder.setPositiveButton("Sim",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        //Verifica se a pagina anterior estava sendo editada
                        if (veioEdicao.equals("1")) { // se sim, manda um put extra avisando para continuar a edição
                            Intent intent = new Intent(CadastrarConsumidores.this, CadastrarEquipamento.class);
                            intent.putExtra("USERTELA", "EDITAR");
                            CadastrarConsumidores.this.startActivity(intent);
                            finish();
                        } else { // se não, envia os dados que estavam sendo inseridos(Caso haja)
                            Intent intent = new Intent(CadastrarConsumidores.this, CadastrarEquipamento.class);
                            intent.putExtra("USERTELA", "10");
                            intent.putExtra("tipo", tipoEquipamento);
                            intent.putExtra("placa", numeroPlacaEquipamento);
                            intent.putExtra("descricao", descricao);
                            intent.putExtra("tensao", tensao);

                            CadastrarConsumidores.this.startActivity(intent);
                            finish();
                        }
                        CadastrarConsumidores.super.onBackPressed();
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
