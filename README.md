# 📌 Conquest

Gioco di strategia a turni, ibrido tra gestione delle città e battaglie con i dadi, con interfaccia grafica JavaFX. Ogni giocatore guida una fazione storica con un leader (con passiva e debolezza), costruisce città, recluta eserciti e li muove sulla mappa contro altri giocatori o contro bot. Si può vincere a punti (massimo 30 round) o per conquista totale, e la partita si può salvare e ricaricare.

Le regole complete, le classi e la persistenza sono descritte nella **Wiki del repository**.

---

## 🚀 Come eseguire il progetto

### Prerequisiti
- Java 21
- Gradle (non serve installarlo: si usa il wrapper `./gradlew` incluso nel repository)

### Istruzioni

```bash
git clone https://github.com/alessandrmancini/gioco.di.ruolo.git
cd gioco.di.ruolo
```

### Build del progetto
```bash
./gradlew build
```

### Esecuzione
```bash
./gradlew run
```

Su Windows usare `gradlew.bat build` e `gradlew.bat run`.

---

## 🤖 Uso di strumenti di AI

Utilizzato Perplexity per:
* generazione della mappa

Utilizzato Claude per:

* comprendere al meglio i concetti del principio SOLID e applicarli al codice 
* aiuto in errori di compilazione
* studio di JavaFX
* eliminazione di codice superfluo
* ottimizzazione di alcuni metodi



---