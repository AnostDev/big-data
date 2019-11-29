What is the difference between `'`, `""` and `$""` for querying in SparkSQL ?

In sparkSQL we can use either string, or Column to make a query, however I noticed that it doesn't always return the same
values.

I have this `json` data in one line in a file.

```json
{"statuses":[ {"created_at": "Thu Nov 21 12:00:00 +0000 2015", "id": 1197665997374836737,"id_str": "id-str-sample","text": "This is a sample text","truncated": false}],"search_metadata":{"completed_in":0.078,"max_id":15201,"max_id_str":"5213","next_results":"sample","query":"A sample query","refresh_url":"sample","count":0, "since_id":0,"since_id_str":"0"}}
```
```scala
val jsonData = spark.read.json("/file/to/json")
```
While
```scala
jsonDataprintSchema
```
```scala
root
 |-- search_metadata: struct (nullable = true)
 |    |-- completed_in: double (nullable = true)
 |    |-- count: long (nullable = true)
 |    |-- max_id: long (nullable = true)
 |    |-- max_id_str: string (nullable = true)
 |    |-- next_results: string (nullable = true)
 |    |-- query: string (nullable = true)
 |    |-- refresh_url: string (nullable = true)
 |    |-- since_id: long (nullable = true)
 |    |-- since_id_str: string (nullable = true)
 |-- statuses: array (nullable = true)
 |    |-- element: struct (containsNull = true)
 |    |    |-- created_at: string (nullable = true)
 |    |    |-- id: long (nullable = true)
 |    |    |-- id_str: string (nullable = true)
 |    |    |-- text: string (nullable = true)
 |    |    |-- truncated: boolean (nullable = true)
```


```scala
val tweets = jsonData.select('statuses)
tweets.printSchema
```

```scala
root
 |-- statuses: array (nullable = true)
 |    |-- element: struct (containsNull = true)
 |    |    |-- created_at: string (nullable = true)
 |    |    |-- id: long (nullable = true)
 |    |    |-- id_str: string (nullable = true)
 |    |    |-- text: string (nullable = true)
 |    |    |-- truncated: boolean (nullable = true)
```
I get the same results with 
```spark
jsonData.select("statuses").printSchema
```
and
```scala
jsonData.select($"statuses").printSchema
```

Now that's where things get weirdier. I want to get the text data in the `statuses.text` field.

This query gets me an error:
```scala
jsonData.select('statuses.text)
res14: org.apache.spark.sql.DataFrame = [statuses: array<struct<created_at:string,id:bigint,id_str:string,text:string,truncated:boolean>>]

<console>:26: error: value text is not a member of Symbol
       rootTweets.select('statuses.text)

jsonData.select('statuses).select('text)

org.apache.spark.sql.AnalysisException: cannot resolve '`text`' given input columns: [statuses];;
'Project ['text]
+- Project [statuses#7]
   +- Relation[search_metadata#6,statuses#7] json
```

But I can get the text data with

```scala
jsonData.select("statuses.text") or jsonData.select($"statuses.text")

res16: org.apache.spark.sql.DataFrame = [text: array<string>]
```




val jsonData = spark.read.json(file)
