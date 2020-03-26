# clojure-socket-data-collector

A simple TCP socket data collector suitable for collecting data from low processing power units such as Arduino + Ethernet/WiFi boards.

## Usage

Fork a repo and implement your logic. Sorry - no package for now ;)

### JVM

```bash
clj -m clojure-socket-data-collector.main

# Or with JAR-file
clj -A:uberjar
java -jar target/uberjar.jar 
```

### NodeJS

```bash
clj -A:cljs -m shadow.cljs.devtools.cli compile app

# or if you have shadow-cljs installed
shadow-cljs compile app
```

## Manual testing

```bash
nc localhost 6900
```

## License

Copyright © 2020 Piotr Jaszczyk

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
