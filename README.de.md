# DeployButler

DeployButler ist ein JetBrains-IDE-Plugin, das dich beim „Deploy per Git“ unterstützt: wiederkehrende Schritte werden in einen geführten Ablauf gepackt, damit Releases konsistent, nachvollziehbar und mit weniger Fehlerpotenzial entstehen.

---

## Inhaltsverzeichnis

- [Was macht das Plugin, wofür wird es benötigt](#was-macht-das-plugin-wofuer-wird-es-benoetigt)
- [Ausführen](#ausfuehren)
  - [Optionen beim Ausfuehren](#optionen-beim-ausfuehren)
    - [Versions-Tagging (Release-Typ)](#versions-tagging---release-typ)
      - [Automatische Ermittlung der Projektversion](#automatische-ermittlung-der-projektversion)
      - [Wann die automatische Erkennung nicht greift](#wann-die-automatische-erkennung-nicht-greift)
    - [Vorschau-Mode (Dry Run)](#vorschau-mode-dry-run)
    - [Bestätigung vor Deploy](#bestaetigung-vor-deploy)
    - [Rebase statt Merge](#rebase-statt-merge)
- [Einstellungen](#einstellungen)
- [Übersetzungen](#uebersetzungen)
- [Lizenz](#lizenz)

---

## Was macht das Plugin, wofuer wird es benoetigt

DeployButler bündelt typische Git-Schritte rund um einen Release-/Deploy-Prozess in einer klaren, geführten Aktion innerhalb der IDE.

Typische Anwendungsfälle:

- Du möchtest Deploys immer nach dem gleichen Muster durchführen (gleiches Tagging-Schema, gleicher Ziel-Branch, gleiche Checks).
- Du willst vor dem eigentlichen Deploy sehen, *was passieren würde*, bevor irgendetwas verändert wird.
- Du möchtest Fehler vermeiden, die bei manuellen Git-Schritten gerne passieren (falscher Branch, unsauberer Working Tree, falsches Tag, etc.).
- Du hast CI/CD-Workflows, die auf Versions-Tags reagieren (z. B. `v*`) und dadurch automatisch Builds/Artefakte erzeugen (Release-APK, Docker-Images, Pakete, Changelogs, GitHub Releases etc.). DeployButler sorgt dafür, dass Tags konsistent und reproduzierbar erstellt werden, damit der Build-Prozess zuverlässig und ohne manuelle „Tagging-Fehler“ startet.
---

## Ausfuehren

Einfach in der Toolbar auf das Deploy-Action-Icon klicken.
![Deploy-Action](docs/assets/action.png)

---

## Optionen beim Ausfuehren

Beim Start des Deploy-Ablaufs kannst du unterschiedliche Optionen aktivieren, je nachdem wie „sicher“ bzw. wie „automatisch“ der Ablauf sein soll.

---

### Versions-Tagging - Release-Typ

DeployButler kann einen Release-Typ auswählen lassen, um daraus den nächsten Versions-Tag abzuleiten.

Üblicherweise gibt es drei Varianten:

![Release-Typen](docs/assets/versions.png)

- **Revision / Bug-Fix**: für kleine Korrekturen ohne neue Features
- **Feature**: für neue Funktionen mit kompatiblen Änderungen
- **Major**: für größere Änderungen / potenzielle Breaking Changes

Zusätzlich kann ein **Tag-Prefix** genutzt werden (z. B. `v`), damit Tags z. B. als `v1.4.0` statt `1.4.0` erstellt werden.

---

#### Automatische Ermittlung der Projektversion

Zusätzlich kann DeployButler – je nach Projektstruktur – die Versionsnummer automatisch aus einer Projektdatei auslesen und diese als Tag-Vorschlag verwenden.

Wenn eine Versionsnummer automatisch erkannt werden kann, erscheint im Release-Dialog zusätzlich die Option:

- **Tag from project file**

Das ist vor allem dann praktisch, wenn die Version bereits im Projekt gepflegt wird und genau diese Version auch als Git-Tag verwendet werden soll.

Aktuell unterstützt DeployButler dafür folgende Quellen:

- **Gradle**
  - funktioniert, wenn die Version direkt in einer Gradle-Datei als Wert hinterlegt ist
  - typische Fälle sind z. B.:
    - `version = "1.2.3"`
    - `versionName = "1.2.3"` (z. B. bei Android-Projekten)
  - berücksichtigt typische Gradle-Dateien im Projekt-Root sowie auch Android-typische Dateien im `app`-Ordner

- **package.json**
  - funktioniert, wenn im Projekt eine `package.json` mit einem normalen `version`-Eintrag vorhanden ist
  - typischer Fall:
    - `"version": "1.2.3"`

- **composer.json**
  - funktioniert, wenn in der `composer.json` explizit ein `version`-Feld gesetzt ist
  - typischer Fall:
    - `"version": "1.2.3"`
  - Hinweis: Nicht jedes Composer-Projekt pflegt die Version in dieser Datei

- **pom.xml**
  - funktioniert, wenn die Projektversion direkt in der Maven-`pom.xml` steht
  - typischer Fall:
    - `<version>1.2.3</version>`

- **Custom Path + Regex**
  - für Sonderfälle kann in den Einstellungen ein eigener Dateipfad und ein eigener regulärer Ausdruck hinterlegt werden
  - damit lassen sich auch projektspezifische oder ungewöhnliche Versionsformate auswerten

---

#### Wann die automatische Erkennung nicht greift

Die automatische Erkennung ist als praktische Unterstützung gedacht und funktioniert am zuverlässigsten dann, wenn die Version **direkt als fester Wert in einer Datei steht**.

Je nach Build-Setup kann es sein, dass keine Version erkannt wird, z. B. wenn:

- die Version nicht direkt als Text in der Datei steht
- die Version aus Variablen, Properties oder anderen Build-Skripten zusammengesetzt wird
- die verwendete Projektstruktur stark vom Standard abweicht
- in der jeweiligen Projektdatei gar keine eigene Versionsangabe gepflegt wird

In solchen Fällen kann weiterhin ganz normal einer der drei Release-Typen gewählt werden oder alternativ ein eigener Pfad mit Regex konfiguriert werden.

> Hinweise:
> - Welche Version als „nächste“ gilt, hängt vom Tagging-Schema und deinen vorhandenen Tags ab.
> - Tag-Prefix kann in den Einstellungen geändert werden. (Er darf auch leer sein.)
> - Die automatische Versionserkennung ist eine Hilfe für typische Projektstrukturen, keine vollständige Build-Analyse.
> - Wenn eine Version nicht automatisch erkannt wird, bedeutet das nicht zwingend, dass das Projekt keine Version hat. Sie steht dann nur nicht in einer direkt auswertbaren Form vor.

---

### Vorschau-Mode (Dry Run)

Im **Dry Run** wird der Ablauf so ausgeführt, dass keine dauerhaften Änderungen entstehen.

Das ist ideal, wenn du:

- erst prüfen willst, ob alles korrekt konfiguriert ist,
- den geplanten Ablauf nachvollziehen willst,
- oder dir vorab anzeigen lassen möchtest, welche Schritte/Änderungen anstehen.

---

### Bestaetigung vor Deploy

Wenn **Bestätigung vor Deploy** aktiv ist, zeigt DeployButler vor dem Ausführen eine Vorschau an und fragt aktiv nach, ob fortgefahren werden soll.

![Vorschau](docs/assets/preview.png)

Das ist hilfreich, wenn du zwar geführt arbeiten willst, aber vor dem „Point of no return“ nochmal bewusst zustimmen möchtest.

---

### Rebase statt Merge

Wenn **Rebase statt Merge** aktiv ist, wird beim Zusammenführen eher ein Rebase-orientierter Ablauf verwendet (statt klassischem Merge).

Das kann sinnvoll sein, wenn du:

- eine linearere Historie bevorzugst,
- Merge-Commits vermeiden willst,
- oder dein Team-/Repo-Workflow darauf ausgelegt ist.

---

## Einstellungen

DeployButler bietet Settings, um den Ablauf an dein Projekt und deinen Release-Prozess anzupassen:

- **Dry run (nur Vorschau, keine Änderungen)**  
  Führt den Ablauf im Vorschau-Modus aus, ohne dauerhafte Änderungen vorzunehmen.

- **Ziel-Branch**  
  Der Branch, auf den der Deploy- / Release-Prozess ausgerichtet ist (zum Beispiel `main` oder `master`).

- **Remote**  
  Das Git-Remote, das für Fetch- und Push-Vorgänge verwendet wird (typischerweise `origin`).

- **Tag-Prefix**  
  Optionales Prefix für Versions-Tags (zum Beispiel `v` → `v1.2.3`).

- **Rebase statt Merge**  
  Verwendet einen Rebase-orientierten Ablauf statt eines klassischen Merges.

- **Bestätigung vor Deploy (Vorschau-Dialog)**  
  Zeigt vor der Ausführung eine Vorschau an und fragt nach einer Bestätigung.

- **Bevorzugter Version-Detektor**  
  Legt fest, welche Versionsquelle bei der automatischen Projektversions-Erkennung bevorzugt geprüft werden soll.  
  Das ist nützlich, wenn dein Projekt mehrere unterstützte Dateien enthält und DeployButler ein bestimmtes Format zuerst auswerten soll.

- **Benutzerdefinierter Pfad zur Versionsdatei**  
  Ermöglicht es, eine eigene Datei für die Versionserkennung anzugeben.  
  Das ist für nicht standardmäßige Projektstrukturen oder Sonderfälle gedacht, bei denen die Version nicht am üblichen Standardort liegt.

- **Benutzerdefinierter Versions-RegEx**  
  Ermöglicht es, einen eigenen regulären Ausdruck zu hinterlegen, um eine Versionsnummer aus der benutzerdefinierten Datei auszulesen.  
  Das ist hilfreich, wenn dein Projekt die Version in einem eigenen Format speichert, das von den eingebauten Detektoren nicht abgedeckt wird.

---

## Uebersetzungen

DeployButler ist mehrsprachig. Aktuell sind folgende Sprachen enthalten:

- Englisch (default)
- Deutsch
- Spanisch
- Französisch
- Italienisch
- Japanisch
- Koreanisch
- Niederländisch
- Polnisch
- Portugiesisch
- Russisch
- Türkisch
- Chinesisch (vereinfacht)

Contributions sind sehr willkommen — besonders für:

- Korrekturen an bestehenden Übersetzungen
- zusätzliche Sprachen
- einheitliche Begrifflichkeiten (z. B. „Deploy“, „Release“, „Preview“, etc.)

---

## Lizenz

[`MIT`](./LICENSE)