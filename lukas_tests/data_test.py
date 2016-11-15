
# coding: utf-8

import numpy as np
import math

f = open("data/leberExport.vol1.vol","r") # Öffne die Datei
f.read(291) # Überspringe die ersten 291 Bytes

data       = f.read(2) # Schlüsselwort / Tag
tag        = np.frombuffer(data, np.uint8)[0] # interpretiere als unsigned short (2 Byte)

print type(data)
print int(tag)

data       = f.read(4) # Größe des Datensatzes
tag_length = np.frombuffer(data, np.uint32)[0] # interpretiere als unsigned long (4 Byte)

print "Sind wir an der richtigen Stelle?", tag == int(0x0050) # Tag: 80 = int(0x0050)
print "Die Größe des Datensatzes in Bytes:", tag_length

data   = f.read(tag_length) # 3D Datensatz auslesen
# Z Y X

VOLUME = np.frombuffer(data, np.uint8).reshape(616,330,410)#interpretiere als unsigned short
