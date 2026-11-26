#!/usr/bin/env sh

#-e beendet das skript beim fehler, -u bricht ab, wenn auf eine undefinierte variable zugegriffen wird
set -eu

echo "starte main datei"

#start die komplimierte klasse HelloWorld mit dem classpath .:lib/*, "&" am ende sagt, dass das prozess im hintergrund läuft
cd /javaapp
exec java -cp "class-dateien:lib/*" SimplePostServer &

#speicher die prozessID des zuletzt im hintergrund gestarteten prozesses in der variable PID1
PID1=$!

echo "start fileuploader"
exec java -cp "class-dateien:lib/*" FileUploader &
JOB_PID=$!

#wartet bis der prozess mit der ID aus JOB_PID beendet wird, um dessen Exit-Code zu übernehmen
wait "$JOB_PID"

#sichert den exit-code des beendeten FileUploader-prozesses in der variable JOB_EXIT
JOB_EXIT=$?

#prüft ob FileUploader mit einem Fehler beendet wurde
if [ "$JOB_EXIT" -ne 0 ]; then
  echo "Warnung: Zweiter Prozess wurde mit Code $JOB_EXIT beendet."
#fi -> schließt den if-block ab
fi

echo "Server läuft"
#wartet auf den prozess mit der ID aus PID1, damit das skript und Container aktiv bleibt, solange der server läuft
wait "$PID1"


#erste zeile -> shebang. weist das BS an, dass es das skript mit der standard-shell ausführen muss, die über env gefunden wird