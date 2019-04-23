# naga-store

This library contains the protocol necessary for integrating a graph database into Naga.

[![Clojars Project](http://clojars.org/org.clojars.quoll/naga-store/latest-version.svg)](http://clojars.org/org.clojars.quoll/naga-store)

## Usage

Import/require the naga-store.core namespace and implement the Storage protocol according
to the documentation on each function.

Also includes some utility functions that use the protocol:
- `assert-schema` multi-arity function for asserting schema without all options.
- `retrieve-contents` Reads all triples from a graph, if this is supported.
- `node-label` Creates a keyword for representing an anonymous node in a graph, if storage needs this.

## License

Copyright © 2018 Cisco

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
