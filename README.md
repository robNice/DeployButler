# DeployButler

DeployButler is a JetBrains IDE plugin that supports you with “deploy via Git”: recurring steps are bundled into a guided flow so that releases are consistent, traceable, and created with fewer opportunities for mistakes.

---

## Table of Contents

- [What the plugin does and why you need it](#what-the-plugin-does-and-why-you-need-it)
- [Run options](#run-options)
  - [Version tagging (release type)](#version-tagging-release-type)
  - [Preview mode (dry run)](#preview-mode-dry-run)
  - [Confirmation before deploy](#confirmation-before-deploy)
  - [Rebase instead of merge](#rebase-instead-of-merge)
- [Settings](#settings)
- [Translations](#translations)
- [License](#license)

---

## What the plugin does and why you need it

DeployButler bundles typical Git steps around a release/deploy process into a clear, guided action inside the IDE.

Typical use cases:

- You want to run deploys following the same pattern every time (same tagging scheme, same target branch, same checks).
- You want to see *what would happen* before the actual deploy, before anything is changed.
- You want to avoid mistakes that commonly happen with manual Git steps (wrong branch, dirty working tree, wrong tag, etc.).
- You have CI/CD workflows that react to version tags (e.g. `v*`) and automatically produce builds/artifacts (release APKs, Docker images, packages, changelogs, GitHub releases, etc.). DeployButler ensures tags are created consistently and reproducibly — so the build process starts reliably without manual “tagging mistakes”.

---


## Running it

Just click on the DeployButler icon in the status bar:

![Deploy-Action](docs/assets/action.png)

## Run options

When starting the deploy flow, you can enable different options depending on how “safe” vs. how “automatic” you want the flow to be.

### Version tagging (release type)

DeployButler can let you choose a release type to derive the next version tag.

Typically there are three variants:

![Release types](docs/assets/versions.png)

<img src="docs/assets/versions.png" alt="Release types" width="600">

- **Revision / bug-fix**: for small fixes without new features
- **Feature**: for new functionality with compatible changes
- **Major**: for bigger changes / potential breaking changes

Additionally, a **tag prefix** can be used (e.g. `v`) so tags are created as `v1.4.0` instead of `1.4.0`.

> Notes:
> - Which version is considered “next” depends on your tagging scheme and your existing tags.
> - The tag prefix can be changed in the settings. (It may also be empty.)

### Preview mode (dry run)

In **dry run**, the flow is executed in a way that no permanent changes are made.

This is ideal if you want to:

- first check that everything is configured correctly,
- understand the planned flow,
- or see in advance which steps/changes are coming.

### Confirmation before deploy

If **confirmation before deploy** is enabled, DeployButler shows a preview before execution and explicitly asks whether it should proceed.

![Preview](docs/assets/preview.png)

This is helpful if you want a guided flow, but still want to consciously confirm before the “point of no return”.

### Rebase instead of merge

If **rebase instead of merge** is enabled, a more rebase-oriented approach is used for integrating changes (instead of a classic merge).

This can make sense if you:

- prefer a more linear history,
- want to avoid merge commits,
- or your team/repo workflow is built around it.

---

## Settings

DeployButler provides settings to adapt the flow to your workflow:

- **Dry run (preview only, no changes)**  
  Runs the flow in preview mode.

- **Target branch**  
  The branch the deploy/release process is aligned with (e.g. `main`, `master`, `live`).

- **Remote**  
  The Git remote used for fetch/push (typically `origin`).

- **Tag prefix**  
  Optional prefix for version tags (e.g. `v` → `v1.2.3`).

- **Rebase instead of merge**  
  Uses a rebase-oriented flow instead of merge.

- **Confirmation before deploy (preview dialog)**  
  Shows a preview before execution and asks for confirmation.

---

## Translations

DeployButler is multilingual. Currently the following languages are included:

- English (default)
- German
- Spanish
- French
- Italian
- Japanese
- Korean
- Dutch
- Polish
- Portuguese
- Russian
- Turkish

Contributions are very welcome — especially for:

- corrections to existing translations
- additional languages
- consistent terminology (e.g. “deploy”, “release”, “preview”, etc.)

If you want to improve something: just open a PR with the updated language files.

---

## License

[`MIT`](./LICENSE)