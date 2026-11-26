#openjdk Version benutzen, legt ein basis-image für den container fest (ein linux image mit installierem JDK)
FROM eclipse-temurin:25-jdk 
 
#arbeitsverzeichniss im container festlegen
#legt /javaapp als aktuelle arbeitsverzeichniss im container fest. alle run, copy usw befehle beziehen sich darauf
WORKDIR /javaapp

#java dateien und Bibliotheken kopieren
#kopiert alle java, jar und startskript in das arbeitsverzeichnis im container
COPY java-dateien/ ./java-dateien/
COPY lib/ ./lib/
COPY html/ ./html/
COPY images/ ./images/
COPY bilds/ ./bilds/
COPY bilds_medals/ ./bilds_medals/
COPY uploads/ ./uploads/
COPY docker/start.sh ./start.sh
 
#run führt beim bauen des images einen shell-befehl aus: 
#sed -i 's/\r$//' start.sh entfernt /r
# && chmod +x start.sh macht start.sh ausfürbar, wenn sed erfolgreich war
RUN sed -i 's/\r$//' start.sh && chmod +x start.sh

#komplimiert alle kopierten java dateien zu den class dateien
RUN mkdir -p class-dateien db && chmod +x ./start.sh && javac -cp ".:lib/*" -d class-dateien java-dateien/*.java

#legt fest, dass die anwendung im container port 8000 nutzt
EXPOSE 8000

#legt fest, dass beim start start.sh als hauptprozess ausgeführt werden muss
ENTRYPOINT [ "./start.sh" ]