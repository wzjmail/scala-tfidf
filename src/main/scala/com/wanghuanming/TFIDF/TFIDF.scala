package com.wanghuanming

import java.io.File
import java.io.PrintWriter
import scala.Array.canBuildFrom
import scala.io.Source
import org.ansj.splitWord.analysis.BaseAnalysis
import org.ansj.splitWord.analysis.ToAnalysis

object TFIDF {
  val pathPrefix = "src/main/resources/"
  val stopwords = List("engStopwords.txt", "zhStopwords.txt").flatMap(file => Source.fromFile(pathPrefix + file).getLines.map(_.trim))

  /**
    * return : List[String, Double] = List(word, freq)
    */

  def getKeywords(content: String, topN: Int = 10, corpus: String) = {
    val allWords = ToAnalysis.parse(content).toArray.map(_.toString.split("/")).filter(_.length >= 1).map(_(0)).toList
    val tf = TF(allWords.filter { word => word.length >= 2 && !stopwords.contains(word) })
    val idf = IDF(corpus)
    val tfidf = tf.map { item =>
      // word -> frequency
      item._1 -> item._2 * idf.getOrElse(item._1, 8.8)
    }.toList
    tfidf.sortBy(_._2).reverse.take(topN)
  }

  def IDF(corpus: String): Map[String, Double] = {
    if (new File(pathPrefix + "IDF.cache").exists) {
      return Source.fromFile(pathPrefix + "IDF.cache").getLines.map(_.split(" ")).filter(_.length == 2)
        .map { item => item(0) -> item(1).toDouble }.toMap
    }
    val files = new File(corpus).listFiles
    val fileCnt = files.length
    val writer = new PrintWriter(pathPrefix + "IDF.cache")
    val res = files.map { file =>
      ToAnalysis.parse(Source.fromFile(file).getLines.reduce(_ + _))
        .toArray.map(_.toString.split("/")(0)).toList.distinct
    }.flatten.groupBy(x => x).map { item => item._1 -> Math.log(fileCnt * 1.0 / (item._2.length + 1)) }
    res.foreach { item => writer.println(item._1 + " " + item._2) }
    writer.close
    return res
  }

  def TF(article: List[String]) = {
    val sum = article.length
    article.groupBy(x => x).map {
      item =>  item._1 -> item._2.length * 1.0 / sum 
    }
  }
}
