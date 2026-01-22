# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

DropConfirm is a Hytale server plugin that requires players to press drop twice to confirm dropping items. It uses the HyUI library for building configuration UIs.

## Build Commands

```bash
# Build the plugin JAR
mvn package

# Clean and rebuild
mvn clean package

# Install to local Maven repository
mvn install
```

The built JAR will be in `target/DropConfirm-<version>.jar`.

## Architecture

### Plugin Entry Point
- `DropConfirm.java` - Main plugin class extending `JavaPlugin`. Handles initialization, config registration, and provides static access to plugin instance.

### Core Components

**Event System** (`events/`)
- `CancelDropEvent` - EntityEventSystem that intercepts `DropItemEvent.PlayerRequest`. Uses a confirmation pattern: first drop attempt cancels and shows warning, second attempt within timeout allows the drop.

**Commands** (`commands/`)
- `DropConfirmCommand` - `/dropconfirm` toggle for players
- `DropConfirmConfigCommand` - `/dropconfirmconfig` opens HyUI settings page

**Configuration** (`config/`)
- `DropConfirmConfig` - Global server config using Hytale's Codec system
- `PlayerPreferences` - Per-player settings stored separately, keyed by UUID

**UI** (`ui/`)
- `DropConfirmConfigPage` - HyUI-based settings page using HTML/CSS-like syntax (HYUIML)

### Key Patterns

**Codec-based Configuration**: All configs use `BuilderCodec` pattern for serialization:
```java
public static final BuilderCodec<MyConfig> CODEC = BuilderCodec
    .builder(MyConfig.class, MyConfig::new)
    .addField(new KeyedCodec<>("FieldName", Codec.TYPE), setter, getter)
    .build();
```

**Event Systems**: Register via `getEntityStoreRegistry().registerSystem()` in `setup()`. Events extend `EntityEventSystem<EntityStore, EventType>`.

**Commands**: Extend `AbstractPlayerCommand` for player-only commands. Register via `getCommandRegistry().registerCommand()`.

**HyUI Pages**: Use `PageBuilder.pageForPlayer()` with HYUIML (HTML-like syntax) and event listeners.

## Dependencies

- `HytaleServer-parent` - Hytale server API (provided scope)
- `HyUI` - UI library from CurseMaven (provided scope, dependency declared in manifest.json)
