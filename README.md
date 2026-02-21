# DeployButler

DeployButler ist ein JetBrains-IDE-Plugin, das dich beim „Deploy per Git“ unterstützt: wiederkehrende Schritte werden in einen geführten Ablauf gepackt, damit Releases konsistent, nachvollziehbar und mit weniger Fehlerpotenzial entstehen.

---

## Inhaltsverzeichnis

- [Was macht das Plugin, wofür wird es benötigt](#was-macht-das-plugin-wofür-wird-es-benötigt)
- [Optionen beim Ausführen](#optionen-beim-ausführen)
    - [Versions-Tagging (Release-Typ)](#versions-tagging-release-typ)
    - [Vorschau-Mode (Dry Run)](#vorschau-mode-dry-run)
    - [Bestätigung vor Deploy](#bestätigung-vor-deploy)
    - [Rebase statt Merge](#rebase-statt-merge)
- [Settings](#settings)
- [Übersetzungen](#übersetzungen)
- [Lizenz](#lizenz)

---

## Was macht das Plugin, wofür wird es benötigt

DeployButler bündelt typische Git-Schritte rund um einen Release-/Deploy-Prozess in einer klaren, geführten Aktion innerhalb der IDE.

Typische Anwendungsfälle:

- Du möchtest Deploys immer nach dem gleichen Muster durchführen (gleiches Tagging-Schema, gleicher Ziel-Branch, gleiche Checks).
- Du willst vor dem eigentlichen Deploy sehen, *was passieren würde*, bevor irgendetwas verändert wird.
- Du möchtest Fehler vermeiden, die bei manuellen Git-Schritten gerne passieren (falscher Branch, unsauberer Working Tree, falsches Tag, etc.).
- Du hast CI/CD-Workflows, die auf Versions-Tags reagieren (z. B. `v*`) und dadurch automatisch Builds/Artefakte erzeugen (Release-APK, Docker-Images, Pakete, Changelogs, GitHub Releases etc.). DeployButler sorgt dafür, dass Tags konsistent und reproduzierbar erstellt werden – damit der Build-Prozess zuverlässig und ohne manuelle „Tagging-Fehler“ startet.
---

## Optionen beim Ausführen

Beim Start des Deploy-Ablaufs kannst du unterschiedliche Optionen aktivieren, je nachdem wie „sicher“ bzw. wie „automatisch“ der Ablauf sein soll.

### Versions-Tagging (Release-Typ)

DeployButler kann einen Release-Typ auswählen lassen, um daraus den nächsten Versions-Tag abzuleiten.

Üblicherweise gibt es drei Varianten:

![Release-Typen](./docs/assets/versions.png)
![Release-Typen](/docs/assets/versions.png)
![Release-Typen](docs/assets/versions.png)
<img src="docs/assets/versions.png" alt="Release-Typen" width="600">

- **Revision / Bug-Fix**: für kleine Korrekturen ohne neue Features
- **Feature**: für neue Funktionen mit kompatiblen Änderungen
- **Major**: für größere Änderungen / potenzielle Breaking Changes

Zusätzlich kann ein **Tag-Prefix** genutzt werden (z. B. `v`), damit Tags z. B. als `v1.4.0` statt `1.4.0` erstellt werden.

> Hinweis: Welche Version als „nächste“ gilt, hängt vom Tagging-Schema und deinen vorhandenen Tags ab.

### Vorschau-Mode (Dry Run)

Im **Dry Run** wird der Ablauf so ausgeführt, dass keine dauerhaften Änderungen entstehen.

Das ist ideal, wenn du:

- erst prüfen willst, ob alles korrekt konfiguriert ist,
- den geplanten Ablauf nachvollziehen willst,
- oder dir vorab anzeigen lassen möchtest, welche Schritte/Änderungen anstehen.

### Bestätigung vor Deploy

Wenn **Bestätigung vor Deploy** aktiv ist, zeigt DeployButler vor dem Ausführen eine Vorschau an und fragt aktiv nach, ob fortgefahren werden soll.

Das ist hilfreich, wenn du zwar geführt arbeiten willst, aber vor dem „Point of no return“ nochmal bewusst zustimmen möchtest.

### Rebase statt Merge

Wenn **Rebase statt Merge** aktiv ist, wird beim Zusammenführen eher ein Rebase-orientierter Ablauf verwendet (statt klassischem Merge).

Das kann sinnvoll sein, wenn du:

- eine linearere Historie bevorzugst,
- Merge-Commits vermeiden willst,
- oder dein Team-/Repo-Workflow darauf ausgelegt ist.

---

## Settings

DeployButler bietet Settings, um den Ablauf an deinen Workflow anzupassen:

- **Dry run (nur Vorschau, keine Änderungen)**  
  Führt den Ablauf im Vorschau-Modus aus.

- **Ziel-Branch**  
  Der Branch, auf den der Deploy-/Release-Prozess ausgerichtet ist (z. B. `main`, `master`, `live`).

- **Remote**  
  Das Git-Remote, das für Fetch/Push verwendet wird (typisch `origin`).

- **Tag-Prefix**  
  Optionales Prefix für Versions-Tags (z. B. `v` → `v1.2.3`).

- **Rebase statt Merge**  
  Nutzt einen Rebase-orientierten Ablauf statt Merge.

- **Bestätigung vor Deploy (Vorschau-Dialog)**  
  Zeigt vor dem Ausführen eine Vorschau und fragt nach Zustimmung.

---

## Übersetzungen

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

Contributions sind sehr willkommen — besonders für:

- Korrekturen an bestehenden Übersetzungen
- zusätzliche Sprachen
- einheitliche Begrifflichkeiten (z. B. „Deploy“, „Release“, „Preview“, etc.)

Wenn du etwas verbessern willst: einfach einen PR mit den angepassten Sprachdateien erstellen.

---

## Lizenz

MIT License