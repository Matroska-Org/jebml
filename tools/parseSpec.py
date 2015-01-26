import xml.etree.ElementTree as ET
import sys
tree = ET.parse(sys.argv[1])
root = tree.getroot()

typeMap = {
    "binary" : "BinaryElement",
    "integer" : "SignedIntegerElement",
    "uinteger" : "UnsignedIntegerElement",
    "master" : "MasterElement",
    "utf-8" : "UTF8StringElement",
    "float" : "FloatElement",
    "date" : "DateElement",
    "string" : "StringElement"
}

print """package org.ebml.matroska;
/** Matroska spec generated element list document
    Do not manually edit this file.
*/
import org.ebml.ProtoType;
import org.ebml.BinaryElement;
import org.ebml.SignedIntegerElement;
import org.ebml.UnsignedIntegerElement;
import org.ebml.MasterElement;
import org.ebml.UTF8StringElement;
import org.ebml.StringElement;
import org.ebml.FloatElement;
import org.ebml.DateElement;

public final class MatroskaDocTypes
{
"""
elementFormat = '  public static final ProtoType<{0}> {1} = new ProtoType<>({0}.class, "{1}", {2}, {3});'
for element in root.iter("element"):
    elemId = element.attrib['id']
    byteArray = []
    for i in range(1,len(elemId)/2):
        byteArray.append("(byte) 0x"+ elemId[2*i:2*i+2])
    byteString = "new byte[] {" + ', '.join(byteArray) + " }"
    elemName = element.attrib['name'].replace('-','_')
    print elementFormat.format(typeMap[element.attrib['type']], elemName, byteString, element.attrib['level'])

print """
  private MatroskaDocTypes()
  {
  }
}"""
