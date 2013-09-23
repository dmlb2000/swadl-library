#!/bin/bash

htmldoc --webpage -f "${1}.pdf" $(find dist/javadoc -name '*.html')
