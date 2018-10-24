package br.desenvolvedor.michelatz.aplicativohcc.conexaoWeb;

public class Config {

    //Servidor real
    //public static final String URL_ADD_IMAGEM ="http://idealizaweb.com.br/dados/inseriFotoServidor.php";
    //public static final String URL_ADD_TXT ="http://idealizaweb.com.br/dados/inseriTxtServidor.php";

    //Servidor teste
    //public static final String URL_ADD_IMAGEM = "http://projetosw.esy.es/dados/inseriFotoServidor.php";
    //public static final String URL_ADD_TXT = "http://projetosw.esy.es/dados/inseriTxtServidor.php";

    public static final String URL_ADD_IMAGEM ="http://idealizaweb.com.br/dadosRGE/inseriFotoServidor.php";
    public static final String URL_ADD_TXT ="http://idealizaweb.com.br/dadosRGE/inseriTxtServidor.php";


    //Chaves que seram usadas nos scripts PHPs
    //Chaves das Imagens
    public static final String KEY_ADD_NUMERO_NOTA = "nota";
    public static final String KEY_ADD_TIPO_IMAGEM = "tipoimagem";
    public static final String KEY_ADD_IMAGEM = "imagem";

    //Chaves do TXT
    public static final String KEY_ADD_NOME_ARQUIVO = "nomearquivo";
    public static final String KEY_ADD_BASE64_ARQUIVO = "arquivo";

    //Tags JSON
    //Tag com o resultado do servidor
    public static final String TAG_JSON_ARRAY = "result";

    //Id da Locaçao que sera passada por intent
    public static final String EMP_ID = "emp_id";
}
