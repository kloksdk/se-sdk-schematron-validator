# SDK Schematron validator för XHE Payloads

Denna Domibus modul innehåller en schematron validator för XHE payloads. Modulen är baserad på schematron reglerna som finns i federationen Säkerdigital  kommunikation.

## Förutsättningar
* Maven

## Bygg plugin jar-fil
mvn clean install

## Användning
För att installera en anpassad valideringsutökning i Domibus, följ stegen nedan:
1. Stoppa Domibus;
2. Kopiera jar-filen till plugins mappen:
   ${domibus.config.location}/extensions/lib
3. Starta Domibus.

## Utveckling
Detta är första versionen, så det finns säkert saker som kan förbättras. 

Idéer på förbättringar. 
* Man ska kunna uppdatera reglerna utan att starta om. (i dagsläget är reglerna inbäddade i JAR-filen) 
* Konfigurera var reglerna ska ligga

## Länkar
* [Domibus](https://ec.europa.eu/digital-building-blocks/wikis/display/DIGITAL/Domibus)
* [Säkerdigital kommunikation](https://www.digg.se/digitala-tjanster/saker-digital-kommunikation-sdk)
* [Schematron](http://schematron.com/)