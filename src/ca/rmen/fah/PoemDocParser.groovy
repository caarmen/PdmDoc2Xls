package ca.rmen.fah

/**
 * Created by calvarez on 28/02/15.
 */
class PoemDocParser {

    private static String extractPageId(String input) {
        def pattern = ~/^([a-z]) *[–-] ([A-Za-z]*) de ([a-z]+) de ([0-9]{4})/
        def matcher = pattern.matcher(input)
        if (matcher.matches())
            return matcher.group(4) + "-" + matcher.group(3) + "-" + matcher.group(1)
        return null
    }

    private static String extractBreveriaId(String input) {
        def pattern = ~/^[0-9]{4}$/
        def matcher = pattern.matcher(input.trim())
        if (matcher.matches())
            return input
        return null
    }

    private static String[] extractSonnetId(String input) {
        def pattern = ~/^([0-9]{4}) [–-] (.*)$/
        def matcher = pattern.matcher(input.trim())
        if (matcher.matches())
            return [matcher.group(1), matcher.group(2)]
        return null
    }

    private static String[] extractLocationDate(String input) {
        def pattern = ~/^([\p{L} ]+), ([0-9]+) de (\p{L}*) de ([0-9]{4})/
        def matcher = pattern.matcher(input)
        if (matcher.matches())
            return [matcher.group(1), matcher.group(4) + "-" + matcher.group(3) + "-" + matcher.group(2)]
        return null
    }

    static Poem[] parse(String textFileName) {
        def file = new File(textFileName)
        def poems = []
        file.withReader { reader ->
            def line
            Poem curPoem
            String curPageId
            while ((line = reader.readLine()) != null) {
                String pageId = extractPageId(line)
                // This is the beginning of a new page
                if (pageId != null) {
                    curPageId = pageId
                } else if (curPageId != null) {
                    // This is a new breveria
                    String breveriaId = extractBreveriaId(line)
                    if (breveriaId != null) {
                        curPoem = new Poem()
                        curPoem.type = Poem.PoemType.BREVERIA
                        curPoem.id = breveriaId
                        curPoem.title = "Brevería ${breveriaId}"
                        curPoem.pageId = curPageId
                        poems.add(curPoem)
                    } else {
                        // This is a new sonnet
                        String[] sonnetId = extractSonnetId(line)
                        if (sonnetId != null) {
                            curPoem = new Poem()
                            curPoem.type = Poem.PoemType.SONNET
                            curPoem.id = sonnetId[0]
                            curPoem.title = sonnetId[1]
                            curPoem.pageId = curPageId
                            poems.add(curPoem)
                        } else if (curPoem != null) {
                            // This is the timestamp of the poem
                            // (the end of the poem)
                            String[] locationDate = extractLocationDate(line)
                            if (locationDate != null) {
                                curPoem.location = locationDate[0]
                                curPoem.date = locationDate[1]
                                curPoem = null
                            } else if (curPoem != null) {
                                // This is content of the poem
                                curPoem.content += line + "\n"
                            }
                        }
                    }
                }
            }
            for (poem in poems)
                poem.content = poem.content.trim()
        }
        return poems
    }
}
