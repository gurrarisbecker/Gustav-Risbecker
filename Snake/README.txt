```markdown
# RISC-V Snake Game

Detta projekt är en hårdvarunära implementering av det klassiska spelet Snake, skrivet i programmeringsspråket C och Assembly. Applikationen är utvecklad för att köras på en RISC-V-arkitektur och demonstrerar hantering av boot-sekvenser samt avbrottshantering..

## Verktyg
* **Programmeringsspråk:** C och Assembly
* **Arkitektur:** RISC-V

## Projektstruktur och organisation
Projektet består av följande komponenter:

```text
├── snake.c            # Huvudkomponent med spel-loopen och spellogiken
├── boot.S             # Assembly-kod för RISC-V boot-up och avbrottshantering (ISR)
├── dtekv-lib.c        # Biblioteksfunktioner för hårdvaruinteraktion och JTAG UART
├── dtekv-lib.h        # Funktionsdeklarationer och gränssnitt för biblioteket
├── dtekv-script.lds   # Linker-skript för att styra minnesallokeringen i RISC-V ELF-utdatan
├── Makefile           # Automatiserat byggskript för kompilering och länkning
└── COPYING            # Licens- och upphovsrättsvillkor för projektet
