package net.marcoreis.util;

import java.io.File;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

public class Buscador {
    private static Logger logger = Logger.getLogger(Buscador.class);
    private String diretorioDoIndice = System.getProperty("user.home")
            + "/indice-lucene";

    public void buscaComParser(String parametro) {
        try {
            Directory diretorio = new SimpleFSDirectory(new File(
                    diretorioDoIndice));
            // {1}
            IndexReader leitor = DirectoryReader.open(diretorio);
            // {2}
            IndexSearcher buscador = new IndexSearcher(leitor);
            Analyzer analisador = new StandardAnalyzer(Version.LUCENE_47);
            // {3}
            QueryParser parser = new QueryParser(Version.LUCENE_47, "Texto",
                    analisador);
            Query consulta = parser.parse(parametro);
            long inicio = System.currentTimeMillis();
            // {4}
            TopDocs resultado = buscador.search(consulta, 100);
            long fim = System.currentTimeMillis();
            int totalDeOcorrencias = resultado.totalHits;
            logger.info("Total de documentos encontrados:" + totalDeOcorrencias);
            logger.info("Tempo total para busca: " + (fim - inicio) + "ms");
            // {5}
            for (ScoreDoc sd : resultado.scoreDocs) {
                Document documento = buscador.doc(sd.doc);
                logger.info("Caminho:" + documento.get("Caminho"));
                logger.info("Ultima modificacao:"
                        + documento.get("UltimaModificacao"));
                logger.info("Score:" + sd.score);
                logger.info("--------");
            }
            leitor.close();
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public static void main(String[] args) {
        Buscador b = new Buscador();
        String parametro = JOptionPane.showInputDialog("Consulta");
        b.buscaComParser(parametro);
    }
}