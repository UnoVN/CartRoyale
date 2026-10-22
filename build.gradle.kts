plugins {
    java
    id("com.gradleup.shadow") version "8.3.5"
}

group = "me.nhan"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.infernalsuite.com/repository/maven-snapshots/")
    maven("https://repo.infernalsuite.com/repository/maven-releases/")
    maven("https://repo.nexomc.com/releases")
    maven("https://mvn.lumine.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("com.infernalsuite.asp:api:4.0.0-SNAPSHOT")
    compileOnly("com.nexomc:nexo:1.7.0")
    compileOnly("com.ticxo.modelengine:ModelEngine:R4.0.7")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
```

### File 4: `src/main/resources/plugin.yml`
```yaml
name: CartBR
version: '1.0.0'
main: me.nhan.cartbr.CartBRPlugin
api-version: '1.21'
authors: [ Nhan ]
description: Cart PvP Battle Royale with ASP Load/Unload, ModelEngine 4, and Nexo
softdepend:
  - ModelEngine
  - Nexo
  - SlimeWorldPlugin
  - SlimeWorldManager

commands:
  cartbr:
    description: Main command for CartBR
    aliases: [ cbr ]
    usage: /cartbr <join|leave|settings|forcestart|setup|reload>

permissions:
  cartbr.admin:
    description: Admin permissions for CartBR setup and control
    default: op
