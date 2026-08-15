from pyspark.sql import SparkSession, Row
import os


if __name__ == '__main__':
    ''' configurare variabila de mediu cu versiunea python utilizata pentru spark '''
    os.environ['PYSPARK_PYTHON'] = '/usr/bin/python3'
    # configurarea si crearea sesiunii Spark SQL
    spark_session = SparkSession\
        .builder\
        .appName("Python Spark SQL example")\
        .config("spark.master", "local")\
        .getOrCreate()

    # initializarea unui DataFrame prin citirea unui json
    ROOT_DIR = os.path.abspath(os.path.dirname(__file__))
    people_json_path = "file:///" + os.path.join(ROOT_DIR, 'resources/people.json')
    df = spark_session.read.json(people_json_path)

    # afisarea continutului din DataFrame la consola
    df.show()

    # Afisarea schemei DataFrame-ului intr-o forma arborescenta
    df.printSchema()

    # Selectarea coloanei nume si afisarea acesteia
    df.select("name").show()

    # Selectarea tuturor datelor si incrementarea varstei cu 1
    df.select(df["name"], df["age"] + 1).show()

    # Selectarea persoanelor cu varsta > 21 ani
    df.filter(df["age"] > 21).show()

    # Numararea persoanelor dupa varsta
    df.groupBy("age").count().show()

    # Inregistarea unui DataFrame ca un SQL View temporar
    df.createOrReplaceTempView("people")
    # din df imi face o tabela normala peste care pot face query uri normale
    # Utilizarea unei interogari SQL pentru a selecta datele
    sqlDF = spark_session.sql("SELECT * FROM people")
    sqlDF.show()

    # Inregistrarea unui DataFrame ca un SQL View global temporar
    df.createGlobalTempView("people")

    ''' Un SQL View global temporar este legat de o baza de date a sistemului: `global_temp` '''
    spark_session.sql("SELECT * FROM global_temp.people").show()

    # Un view global temporar este vizibil intre sesiuni
    spark_session.newSession().sql("SELECT * FROM global_temp.people").show()

    # Interoperabilitatea cu RDD-uri
    # Crearea unui RDD de obiecte Person dintr-un fisier text
    people_txt_path = "file:///" + os.path.join(ROOT_DIR, 'resources/people.txt')
    lines = spark_session.sparkContext.textFile(people_txt_path)
    people = lines.map(lambda l: l.split(",")).map(lambda p: Row(name=p[0], age=int(p[1])))

    ''' Aplicarea unei scheme pe un RDD de bean-uri pentru a obtine DataFrame '''
    peopleDF = spark_session.createDataFrame(people)
    # Inregistrarea DataFrame-ului ca un view temporar
    peopleDF.createOrReplaceTempView("people")

    # Selectarea persoanelor intre 13 si 19 ani cu o interogare SQL
    teenagersDF = spark_session.sql("SELECT name FROM people WHERE age BETWEEN 13 AND 19")

    teen_names = teenagersDF.rdd.map(lambda p: "Name: " + p.name).collect()
    for name in teen_names:
        print(name)
