package ca.rmen.fah

/**
 * Created by calvarez on 28/02/15.
 */
class Main {
    static main(args) {
        def inputFileName = args[0]
        def outputFileName = args[1]
        Poem[] poems = PoemDocParser.parse(inputFileName)
        PoemXlsWriter.write(outputFileName, poems)
    }
}
