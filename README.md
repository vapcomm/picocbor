# picocbor
Minimalistic approach to CBOR encoding

This pure Kotlin multiplatform library brings simplest [CBOR](https://www.rfc-editor.org/rfc/rfc7049) implementation to any Kotlin project.


## Restrictions

- Float numbers encoding uses only IEEE 754 Single-Precision Float.
- Arrays, maps, byte strings, and text strings are fixed lengths only.

