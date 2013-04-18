package net.marcoreis.util;

import java.io.*;
import java.text.*;

import org.apache.log4j.*;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;
import org.apache.lucene.util.*;
import org.apache.tika.*;

/**
* Classe que indexa os arquivos de um diretório
*/
public class Indexador {
  private static Logger logger = Logger.getLogger(Indexador.class);
  private String diretorioDosIndices = System.getProperty("user.home")
      + "/indice-lucene";
  private String diretorioParaIndexar = System.getProperty("user.home")
      + "/Dropbox/MaterialDeEstudo/big-data";
  private IndexWriter writer;
  private Tika tika;

  public static void main(String[] args) {
    Indexador indexador = new Indexador();
    indexador.indexaArquivosDoDiretorio();
  }

  /**
  * Percorre o diretório raíz a procura de arquivos
  */
  public void indexaArquivosDoDiretorio() {
    try {
      File diretorio = new File(diretorioDosIndices);
      apagaIndices(diretorio);
      //{5}
      Directory d = new SimpleFSDirectory(diretorio);
      logger.info("Diretório do índice: " + diretorioDosIndices);
      //{6}
      Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
      //{7}
      IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36,
          analyzer);
      //{8}
      writer = new IndexWriter(d, config);
      long inicio = System.currentTimeMillis();
      indexaArquivosDoDiretorio(new File(diretorioParaIndexar));
      //{9}
      writer.commit();
      writer.close();
      long fim = System.currentTimeMillis();
      logger.info("Tempo para indexar: " + ((fim - inicio) / 1000) + "s");
    } catch (IOException e) {
      logger.error(e);
    }
  }

  /**
  * Apaga os índices existentes em um diretório
  */
  private void apagaIndices(File diretorio) {
    if (diretorio.exists()) {
      File arquivos[] = diretorio.listFiles();
      for (File arquivo : arquivos) {
        arquivo.delete();
      }
    }
  }

  /**
  * Nesta versão indexa arquivos .pdf e .txt
  * Há bibliotecas para extrair texto de documentos do MS Office e OpenOffice
  */
  public void indexaArquivosDoDiretorio(File raiz) {
    FilenameFilter filtro = new FilenameFilter() {
      public boolean accept(File arquivo, String nome) {
        if (nome.toLowerCase().endsWith(".pdf")
            || nome.toLowerCase().endsWith(".odt")
            || nome.toLowerCase().endsWith(".doc")
            || nome.toLowerCase().endsWith(".docx")
            || nome.toLowerCase().endsWith(".ppt")
            || nome.toLowerCase().endsWith(".pptx")
            || nome.toLowerCase().endsWith(".xls")
            || nome.toLowerCase().endsWith(".txt")
            || nome.toLowerCase().endsWith(".rtf")) {
          return true;
        }
        return false;
      }
    };
    for (File arquivo : raiz.listFiles(filtro)) {
      if (arquivo.isFile()) {
        StringBuffer msg = new StringBuffer();
        msg.append("Indexando o arquivo ");
        msg.append(arquivo.getAbsoluteFile());
        msg.append(", ");
        msg.append(arquivo.length() / 1000);
        msg.append("kb");
        logger.info(msg);
        try {
          //{10}
          String textoExtraido = getTika().parseToString(arquivo);
          indexaArquivo(arquivo, textoExtraido);
        } catch (Exception e) {
          logger.error(e);
        }
      } else {
        indexaArquivosDoDiretorio(arquivo);
      }
    }
  }

  /**
  * Preenche os atributos do documento que será indexado
  */
  private void indexaArquivo(File arquivo, String textoExtraido) {
    SimpleDateFormat formatador = new SimpleDateFormat("yyyyMMdd");
    String ultimaModificacao = formatador.format(arquivo.lastModified());
    //{11}
    Document documento = new Document();
    documento.add(new Field("UltimaModificacao", ultimaModificacao,
        Field.Store.YES, Field.Index.NOT_ANALYZED));
    documento.add(new Field("Caminho", arquivo.getAbsolutePath(),
        Field.Store.YES, Field.Index.NOT_ANALYZED));
    documento.add(new Field("Texto", textoExtraido, Field.Store.YES,
        Field.Index.ANALYZED));
    try {
      //{12}
      getWriter().addDocument(documento);
    } catch (IOException e) {
      logger.error(e);
    }
  }

  public Tika getTika() {
    if (tika == null) {
      tika = new Tika();
    }
    return tika;
  }

  public IndexWriter getWriter() {
    return writer;
  }
}