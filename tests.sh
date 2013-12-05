#!/bin/bash

#le fichier de test est lancé sur la machine cliente

#tests réalisés
#  - création de fichiers de taille croissante
#  - lecture des fichiers crées

#création de fichiers de 50Ko, 100Ko, 200Ko, 500Ko, 1Mo, 10Mo, 20Mo, 50Mo, 100Mo, 200Mo 

dd if=/dev/zero of=fichier_50kB bs=1k count=50
dd if=/dev/zero of=fichier_100kB bs=1k count=100
dd if=/dev/zero of=fichier_200kB bs=1k count=200
dd if=/dev/zero of=fichier_500kB bs=1k count=500
dd if=/dev/zero of=fichier_1MB bs=1k count=1000
dd if=/dev/zero of=fichier_10MB bs=1k count=10000
dd if=/dev/zero of=fichier_20MB bs=1k count=20000
dd if=/dev/zero of=fichier_50MB bs=1k count=50000
dd if=/dev/zero of=fichier_100MB bs=1k count=100000
dd if=/dev/zero of=fichier_200MB bs=1k count=200000

var_1k=1000

echo "size of file (bytes); execution time of write(s); throughput (kB/s)" > resultats.csv

(echo -n "51;" && ((time java -jar bobby.jar write fichier_50kB toto) 2>&1 | grep real | cut -d m -f 2 | cut -d s -f 1)) >> resultats.csv
(echo -n "102;" && ((time java -jar bobby.jar write fichier_100kB toto) 2>&1 | grep real | cut -d m -f 2 | cut -d s -f 1)) >> resultats.csv
(echo -n "205;" && ((time java -jar bobby.jar write fichier_200kB toto) 2>&1 | grep real | cut -d m -f 2 | cut -d s -f 1)) >> resultats.csv
(echo -n "512;" && ((time java -jar bobby.jar write fichier_500kB toto) 2>&1 | grep real | cut -d m -f 2 | cut -d s -f 1)) >> resultats.csv
(echo -n "1000;" && ((time java -jar bobby.jar write fichier_1MB toto) 2>&1 | grep real | cut -d m -f 2 | cut -d s -f 1)) >> resultats.csv
(echo -n "10000;" && ((time java -jar bobby.jar write fichier_10MB toto) 2>&1 | grep real | cut -d m -f 2 | cut -d s -f 1)) >> resultats.csv
(echo -n "20000;" && ((time java -jar bobby.jar write fichier_20MB toto) 2>&1 | grep real | cut -d m -f 2 | cut -d s -f 1)) >> resultats.csv
(echo -n "50000;" && ((time java -jar bobby.jar write fichier_50MB toto) 2>&1 | grep real | cut -d m -f 2 | cut -d s -f 1)) >> resultats.csv
(echo -n "100000;" && ((time java -jar bobby.jar write fichier_100MB toto) 2>&1 | grep real | cut -d m -f 2 | cut -d s -f 1)) >> resultats.csv
(echo -n "200000;" && ((time java -jar bobby.jar write fichier_200MB toto) 2>&1 | grep real | cut -d m -f 2 | cut -d s -f 1)) >> resultats.csv

echo ""

echo "size of file (bytes); execution time of read(s); throughput (kB/s)" >> resultats.csv

(echo -n "51;" && ((time java -jar bobby.jar read fichier_50kB toto) 2>&1 | grep real | cut -d m -f 2 | cut -d s -f 1)) >> resultats.csv
(echo -n "102;" && ((time java -jar bobby.jar read fichier_100kB toto) 2>&1 | grep real | cut -d m -f 2 | cut -d s -f 1)) >> resultats.csv
(echo -n "205;" && ((time java -jar bobby.jar read fichier_200kB toto) 2>&1 | grep real | cut -d m -f 2 | cut -d s -f 1)) >> resultats.csv
(echo -n "512;" && ((time java -jar bobby.jar read fichier_500kB toto) 2>&1 | grep real | cut -d m -f 2 | cut -d s -f 1)) >> resultats.csv
(echo -n "1000;" && ((time java -jar bobby.jar read fichier_1MB toto) 2>&1 | grep real | cut -d m -f 2 | cut -d s -f 1)) >> resultats.csv
(echo -n "10000;" && ((time java -jar bobby.jar read fichier_10MB toto) 2>&1 | grep real | cut -d m -f 2 | cut -d s -f 1)) >> resultats.csv
(echo -n "20000;" && ((time java -jar bobby.jar read fichier_20MB toto) 2>&1 | grep real | cut -d m -f 2 | cut -d s -f 1)) >> resultats.csv
(echo -n "50000;" && ((time java -jar bobby.jar read fichier_50MB toto) 2>&1 | grep real | cut -d m -f 2 | cut -d s -f 1)) >> resultats.csv
(echo -n "100000;" && ((time java -jar bobby.jar read fichier_100MB toto) 2>&1 | grep real | cut -d m -f 2 | cut -d s -f 1)) >> resultats.csv
(echo -n "200000;" && ((time java -jar bobby.jar read fichier_200MB toto) 2>&1 | grep real | cut -d m -f 2 | cut -d s -f 1)) >> resultats.csv


