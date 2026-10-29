# NGTStore — by NOTGAMERTHUZ

All-in-one **Store + Auction House + Bounty** plugin for **Paper 26.2**, with full NOTGAMERTHUZ branding.

## Features
- **Store GUI** — categories, paged item lists, buy/sell by click (1 or 64 with Shift), live balance, `/sellall`
- **Auction House** — GUI listings, confirm-purchase menu, "My Listings", cancel, expiry + claim box, sale tax
- **Bounties** — `/bounty set`, head-based GUI sorted by amount, auto payout on player kill, server broadcasts
- **Economy** — uses **Vault** if installed, otherwise a built-in balance system
- Fully configurable prices/categories in `config.yml`

## Commands
| Command | Description |
|---|---|
| `/store` (`/shop`) | Open store |
| `/sellall` | Sell all sellable items |
| `/ah` (`/auction`) | Auction GUI · `/ah sell <price>` · `/ah mine` · `/ah claim` |
| `/bounty` | Bounty GUI · `/bounty set <player> <amount>` · `/bounty check <player>` |
| `/balance` (`/bal`) | Your balance |
| `/ngt reload` / `/ngt eco <give\|take> <player> <amt>` | Admin (`ngtstore.admin`) |

## Build
Requires **Java 25** and Maven.
```
mvn clean package
```
Jar: `target/NGTStore-1.0.0.jar` → drop into `plugins/`.

> Check `paper.api.version` in `pom.xml` against https://docs.papermc.io for the exact Paper 26.2 API version.

## Upload to GitHub
```
git init
git add .
git commit -m "NGTStore v1.0.0 by NOTGAMERTHUZ"
git branch -M main
git remote add origin https://github.com/<your-username>/NGTStore.git
git push -u origin main
```
GitHub Actions (`.github/workflows/build.yml`) builds the jar automatically on every push.
