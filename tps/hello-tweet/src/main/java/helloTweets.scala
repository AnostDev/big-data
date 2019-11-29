import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object helloTweets {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf()
      .setAppName("Sentiment Analysis")
      .setMaster("local[4]")

    val spark = SparkSession.builder().config(conf).getOrCreate()
    val sc = spark.sparkContext
    import spark.implicits._

    print(args.length)
    args.foreach(println)

    val tweetFile = args(0)//"/home/chriss/Desktop/studies/amu/m2/big-data/hello-tweet/src/main/resources/data/trump.json"
    val scoresFile = args(1)//"/home/chriss/Desktop/studies/amu/m2/big-data/hello-tweet/src/main/resources/data/AFINN-111.txt"
    val tweetsRdd = spark.read.json(tweetFile).toDF
    tweetsRdd.describe()

    /**
     *
     */
    val scoreRdd = sc.textFile(scoresFile)
    val scores = scoreRdd.map(_.split("[\\s]+")).map(t => (t(0), t(1)))
    val tweets = tweetsRdd.select("statuses")
    tweets.createOrReplaceTempView("tweets")
    val tweetsText = spark.sql("select id, text from tweets " +
      "lateral view explode(statuses.text) exploded_text as text " +
      "lateral view explode(statuses.id) exploded_id as id") //tweets.select($"statuses.text" as "text", $"statuses.id" as "id")
    tweetsText.show(10)
    
    /*
    val words = rawscore.flatMap(line => {
      val kv = line.split("[\\s]+")
      (kv(0), kv(1))
    })
    */
    //words.foreach(w => println(w))

    /*val rddScore = rawscore.flatMap(l => {
      val k,v = l.split(" ")
      (k,v)
    })*/



    /*val statues = rddTweets.select('statuses)

    val tmp = statues.take(10)
    tmp.foreach(t => {
      print(t)
    })

    print(statues.schema)

    println(rddTweets.schema)*/

    //val text = rddTweets.select('text)

    //println(text)


    spark.close()
  }
}
