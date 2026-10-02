# AuthKit Android Agent Customizations 🤖

Este directorio contiene las personalizaciones de Antigravity (skills, directrices y configuraciones) específicas para el proyecto **AuthKit Android**.

---

## 📚 Catálogo de Skills Especializados

| Skill | Ubicación | Propósito |
| :--- | :--- | :--- |
| **`authkit-security-audit`** | [SKILL.md](skills/authkit-security-audit/SKILL.md) | Auditoría de seguridad: prevención de fugas de credenciales, tokens, uso de `EncryptedSharedPreferences`, Keystore y limpieza de memoria. |
| **`authkit-provider-scaffold`** | [SKILL.md](skills/authkit-provider-scaffold/SKILL.md) | Guía paso a paso para añadir nuevos proveedores de autenticación (Biometría, Social Login, Passkeys, SSO) usando la arquitectura de plugins y DSL. |
| **`authkit-session-verification`** | [SKILL.md](skills/authkit-session-verification/SKILL.md) | Matriz de pruebas y validación del ciclo de vida de sesión, timers, AlarmManager y condiciones de carrera en refresco de tokens (401). |
| **`android-release-pipeline`** | [SKILL.md](skills/android-release-pipeline/SKILL.md) | Checklist y comandos para static analysis (`detekt`), tests unitarios y publicación con Fastlane a GitHub Packages. |

---

## 🌐 Habilitación del Skill Global `android-cli`

El entorno dispone de la definición del skill global `android-cli`, que permite:
* Controlar dispositivos y emuladores (`android emulator`).
* Inspeccionar jerarquías de vistas (`android layout`) y capturar pantallas (`android screen capture`).
* Consultar la base de conocimientos oficial de Android (`android docs search`).

### Instalación en Windows:
Para que las herramientas de línea de comandos de `android` funcionen en tu terminal, ejecuta en PowerShell:

```powershell
curl -fsSL https://dl.google.com/android/cli/latest/windows_x86_64/install.cmd -o "$env:TEMP\i.cmd"; & "$env:TEMP\i.cmd"
```

Una vez instalado, reinicia la terminal o verifica con:
```powershell
android --version
```
