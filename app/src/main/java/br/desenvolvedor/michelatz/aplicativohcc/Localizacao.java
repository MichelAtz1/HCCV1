package br.desenvolvedor.michelatz.aplicativohcc;

import android.Manifest;
import android.app.AlertDialog;
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
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import br.desenvolvedor.michelatz.aplicativohcc.Adapter.AdapterListViewGeral;
import br.desenvolvedor.michelatz.aplicativohcc.ClassesExtras.Helper;
import br.desenvolvedor.michelatz.aplicativohcc.Modelo.DadosGerais;

public class Localizacao extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 200;

    Uri outputUri;
    File fileFinal;
    private String imgString, idPoste, valorPagina = "0", caminhoImagem = "inicial";
    String BANCO = "banco.db", TABELAFOTOSPOSTE = "fotosposte", TABELAPOSTE = "poste";

    private ListView listViewFotosPostes;
    private ArrayList<DadosGerais> itens;
    private AdapterListViewGeral adapterListViewFotosPoste;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localizacao);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Aplicativo HCC");
        toolbar.setSubtitle("Imagem do poste");

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
        idPoste = sharedpreferences.getString("idPosteKey", null);

        listViewFotosPostes = (ListView) findViewById(R.id.listViewFotosPostes);

        // Verifica se recebeu dados da tela anterior
        if (getIntent().getStringExtra("USERTELA") != null) {
            // verifica se é para edição
            if (getIntent().getStringExtra("USERTELA").equals("EDITAR")) {
                valorPagina = "1";
            }
        }
        inflaListaFotosPostes();
    }

    //Gera lista de postes
    private void inflaListaFotosPostes() {
        itens = new ArrayList<DadosGerais>();

        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        // Busca quantidade de fotos na tabela FotosPoste
        Cursor linhas = db.rawQuery("SELECT ID FROM " + TABELAFOTOSPOSTE + " WHERE IDPOSTE = " + idPoste + ";", null);
        int contador = 1;
        if (linhas.moveToFirst()) {
            do {
                String idPoste = linhas.getString(0);
                String texto = "Foto " + contador;

                DadosGerais item1 = new DadosGerais(idPoste, texto);
                itens.add(item1);
                contador++;
            }
            while (linhas.moveToNext());
        }
        adapterListViewFotosPoste = new AdapterListViewGeral(this, itens);
        listViewFotosPostes.setAdapter(adapterListViewFotosPoste);
        listViewFotosPostes.setCacheColorHint(Color.TRANSPARENT);

        linhas.close();
        // Aumenta a quantidade de linhas de acordo com o que tem no banco de dados
        Helper.getListViewSize(listViewFotosPostes);
        db.close();
    }

    // Método que pega a ação do botão "Salvar"
    public void salvarLocalizacao(View v) {

        Toast.makeText(getApplicationContext(), "Inserção de dados bem sucedida!", Toast.LENGTH_SHORT).show();
        SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.remove("idPosteKey");

        editor.commit();
        editor.clear();

        Intent it;
        it = new Intent(this, GerenciarLocacoes.class);
        startActivity(it);
        finish();

    }

    // Método de ação do botão "+" (Tirar nova imagem)
    public void inseriImagem(View v) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Localizacao.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
            return;
        }

        // Verifica versão
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // versão superior a M
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            outputUri = getTempCameraUri(); // busca caminho via URI

            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
            startActivityForResult(intent, 1);
        } else { // versão inferior a M
            Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (it.resolveActivity(getPackageManager()) != null) {
                File arquivoFoto = null;
                try {
                    arquivoFoto = criaArquivoImagem(); //Cria caminho via
                } catch (IOException ex) {

                }
                if (arquivoFoto != null) {
                    it.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(arquivoFoto));
                    startActivityForResult(it, 0);
                }
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(Localizacao.this, "Desculpe!!! você não pode usar este aplicativo sem conceder permissão!", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (resultCode == RESULT_OK) {
                Bitmap bitmap = null;
                if (outputUri == null) {
                    return;
                }
                if (outputUri != null) {
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), outputUri);
                        Bitmap tamanhoReduzidoImagem;
                        tamanhoReduzidoImagem = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * 0.3), (int) (bitmap.getHeight() * 0.3), true);
                        imgString = Base64.encodeToString(getBytesFromBitmap(tamanhoReduzidoImagem), Base64.NO_WRAP);
                        salvaFotoPosteBanco();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Você NÃO inseriu a foto!", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (resultCode == RESULT_OK) {
                File arquivoImagem = new File(caminhoImagem);
                if (arquivoImagem.exists()) {
                    Bitmap imagemBitmap = BitmapFactory.decodeFile(
                            arquivoImagem.getAbsolutePath());

                    Bitmap tamanhoReduzidoImagem, imagemVirada;
                    imagemVirada = RotateBitmap(imagemBitmap, 90);
                    tamanhoReduzidoImagem = Bitmap.createScaledBitmap(imagemVirada, (int) (imagemVirada.getWidth() * 0.5), (int) (imagemVirada.getHeight() * 0.5), true);
                    imgString = Base64.encodeToString(getBytesFromBitmap(tamanhoReduzidoImagem), Base64.NO_WRAP);
                    salvaFotoPosteBanco();
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Você NÃO inseriu a foto!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Método que gira a imagem em 90 graus
    public static Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    // Método de codifica a imagem, para base64
    public static String encodeToBase64(Bitmap image, Bitmap.CompressFormat compressFormat, int quality) {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

    // Método que transforma a imagem em byte[]
    public byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // Cria local onde será salvo a imagem
    private Uri getTempCameraUri() {
        try {
            SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
            // Cria o nome do arquivo com numeroDaNota+NomeDoDocumento+horarioDoSistema
            String numeroNotaSelect = sharedpreferences.getString("numeroNotaKey", null);
            String idLocacao = sharedpreferences.getString("idLocacaoKey", null);
            String horarioSistema = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            //String nomeArquivoImagem = "JPEG_" + numeroNotaSelect + "_" + horarioSistema + "_";
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/HCC/";

            db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
            Cursor linhas = db.rawQuery("SELECT * FROM " + TABELAPOSTE + " WHERE IDLOCACAO = " + idLocacao + ";", null);
            int contador = 0;
            String texto = " ";
            if (linhas.moveToFirst()) {
                do {
                    contador++;
                    if(linhas.getString(0).equals(idPoste)){
                        //Log.d("retorno","Banco: "+linhas.getString(0));
                        //Log.d("retorno","Lista: "+idPoste);
                        texto = "Poste " + contador;
                    }
                }
                while (linhas.moveToNext());
            }
            linhas.close();
            db.close();
            String nomeArquivoImagem = texto+" " + numeroNotaSelect + "_" + horarioSistema + "_";

            // Verifica se a pasta HCC existe, caso não exista, crie o diretório
            File dir = new File(path);
            if (!dir.exists())
                dir.mkdirs();

            // Verifica se a pasta com o numero da nota existe, caso não exista, cria o diretório dentro da pasta HCC
            String path2 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/HCC/" + numeroNotaSelect;
            File dir2 = new File(path2);
            if (!dir2.exists())
                dir2.mkdirs();

            // Salva a imagem no caminho criado
            fileFinal = File.createTempFile(nomeArquivoImagem, ".jpg", dir2);
            Uri photoURI2 = FileProvider.getUriForFile(Localizacao.this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    fileFinal);

            return photoURI2;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Método responsável por pegar ação do botão nativo "Voltar" do smartfone
    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Tem certeza que deseja sair desta aba? Os dados ainda não foram salvos");

        // Verifica se o usuario realmente deseja sair
        // Se sim, volta pra pagina anterior sem salvar os dados
        alertDialogBuilder.setPositiveButton("Sim",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent intent = new Intent(Localizacao.this, CadastrarEquipamento.class);
                        intent.putExtra("USERTELA", "EDITAR");
                        Localizacao.this.startActivity(intent);
                        finish();
                        Localizacao.super.onBackPressed();
                    }
                });

        // Se não, continua na mesma pagina
        alertDialogBuilder.setNegativeButton("Não",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    // Ação do botão excluir
    public void deletaItem(View v) {
        // Utiliza o metodo que esta na classe
        adapterListViewFotosPoste.removeItem((Integer) v.getTag());
        adapterListViewFotosPoste.notifyDataSetChanged();
        // pega o Id da Imagem selecionada
        String idMensagem = adapterListViewFotosPoste.idSelecionado;
        confirmarDelete(idMensagem);
    }

    // Abre um Alert, para confirmar exclusão
    private void confirmarDelete(final String idMensagem) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Tem certeza que deseja deletar esta Foto?");

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

    // Deleta a imagem do Banco de Dados
    private void deletarMensagem(final String idMens) {
        int idExcluido = Integer.parseInt(idMens.toString());
        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        db.execSQL("DELETE FROM " + TABELAFOTOSPOSTE + " WHERE ID = " + idExcluido + "");
        db.close();

        inflaListaFotosPostes();
    }

    // Salva os dados da imagem no banco de dados
    private void salvaFotoPosteBanco() {
        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        db.execSQL("INSERT INTO " + TABELAFOTOSPOSTE + "(IMAGEM, IDPOSTE) VALUES ('" + imgString + "','" + idPoste + "')");
        db.close();
        inflaListaFotosPostes();
        Toast.makeText(getApplicationContext(), "Inserção realizada com sucesso!", Toast.LENGTH_SHORT).show();
    }

    // Ação do botão voltar no toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setMessage("Tem certeza que deseja sair desta aba? Os dados ainda não foram salvos");

                // Verifica se realmente gostaria de sair
                // Se sim, volta para pagina anterior sem salvar nada
                alertDialogBuilder.setPositiveButton("Sim",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                Intent intent = new Intent(Localizacao.this, CadastrarEquipamento.class);
                                intent.putExtra("USERTELA", "EDITAR");
                                Localizacao.this.startActivity(intent);
                                finish();
                                Localizacao.super.onBackPressed();
                            }
                        });

                // Se não, continua na mesma pagina
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

    // Método que cria o caminho para armazenar a imagem
    private File criaArquivoImagem() throws IOException {
        SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
        String numeroNotaSelect = sharedpreferences.getString("numeroNotaKey", null);
        String idLocacao = sharedpreferences.getString("idLocacaoKey", null);
        String horarioSistema = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        //String nomeArquivoImagem = "JPEG_" + numeroNotaSelect + "_" + horarioSistema + "_";

        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        Cursor linhas = db.rawQuery("SELECT * FROM " + TABELAPOSTE + " WHERE IDLOCACAO = " + idLocacao + ";", null);
        int contador = 0;
        String texto = " ";
        if (linhas.moveToFirst()) {
            do {
                contador++;
                if(linhas.getString(0).equals(idPoste)){
                    //Log.d("retorno","Banco: "+linhas.getString(0));
                    //Log.d("retorno","Lista: "+idPoste);
                    texto = "Poste " + contador;
                }
            }
            while (linhas.moveToNext());
        }
        linhas.close();
        db.close();
        String nomeArquivoImagem = texto+" " + numeroNotaSelect + "_" + horarioSistema + "_";

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/HCC/";

        // Verifica se a pasta da HCC existe, caso não exista, cria o diretório
        File dir = new File(path);
        if (!dir.exists())
            dir.mkdirs();

        // Veriifica se a pasta com o numero da nota da locação existe, caso não exista, cria o diretório dentro da pasta da HCC
        String path2 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/HCC/" + numeroNotaSelect;
        File dir2 = new File(path2);
        if (!dir2.exists())
            dir2.mkdirs();

        File imagem = File.createTempFile(nomeArquivoImagem, ".jpg", dir2);
        caminhoImagem = imagem.getAbsolutePath();

        return imagem;
    }

}