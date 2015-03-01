package ca.rmen.pdm.doc2xls

/**
 * Created by calvarez on 28/02/15.
 */
class PoemDocParser {

    private static String extractPageId(String input) {
        def pattern = ~/^([a-z]) *[–-] (\p{L}*) de (\p{L}+) de ([0-9]{4})/
        def matcher = pattern.matcher(input)
        if (matcher.matches())
            return matcher.group(4) + "-" + matcher.group(3) + "-" + matcher.group(1)

        // Older documents (early 2001):
        pattern = ~/^Poemas de (\p{L}+) de ([0-9]{4})/
        matcher = pattern.matcher(input)
        if (matcher.matches())
            return matcher.group(2) + "-" + matcher.group(1)

        // Even older:
        pattern = ~/^.*\((\p{L}+) (199[89])\)\s*$/
        matcher = pattern.matcher(input)
        if (matcher.matches())
            return matcher.group(2) + "-" + matcher.group(1)
        return null
    }

    private static String extractBreveriaId(String input) {
        def pattern = ~/^[0-9]+$/
        def matcher = pattern.matcher(input.trim())
        if (matcher.matches())
            return input
        return null
    }

    private static String[] extractSonnetId(String input) {
        def pattern = ~/^([0-9]+) ?[–-] (.*)$/
        def matcher = pattern.matcher(input.trim())
        if (matcher.matches())
            return [matcher.group(1), matcher.group(2)]
        return null
    }

    private static String[] extractLocationDate(String input) {
        def pattern = ~/^([\p{L} \(\)]+)[\.,]? ([0-9]+) de (\p{L}*) (de )?([0-9]{4})[\. ]*/
        def matcher = pattern.matcher(input)
        if (matcher.matches())
            return [matcher.group(1), matcher.group(5) + "-" + matcher.group(3) + "-" + matcher.group(2)]
        // One bug in 1998:
        pattern = ~/Los Angeles, 21 de diciembre/
        matcher = pattern.matcher(input)
        if (matcher.matches())
            return ["Los Angeles","1998-diciembre-21"]
        return null
    }

    private static void cleanupPoems(List<Poem> poems) {
        for (Poem poem : poems) {
            poem.content = poem.content.trim()
        }
    }

    private static void removeBogusPoems(List<Poem> poems) {
        for (Iterator<Poem> it = poems.iterator(); it.hasNext();) {
            Poem poem = it.next();
            if (poem.type != Poem.PoemType.POEM)
                continue
            // In case we accidentally parsed something that shouldn't
            // be a poem, remove it.
            if (poem.location == null || poem.date == null) {
                println("Removing poem ${poem}")
                it.remove()
                continue
            }

            // We assume real poems will have at least 4 lines of text.
            String content = poem.content.replaceAll(" ", "");
            content = content.replaceAll("[\\n]+", "\n")
            String[] lines = content.split("\n");
            if (lines.length < 4) {
                println("Removing poem ${poem}")
                it.remove()
            }
        }
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
                    String breveriaId = extractBreveriaId(line)
                    if (breveriaId != null) {
                        // This is a new breveria
                        curPoem = new Poem(Poem.PoemType.BREVERIA, breveriaId, curPageId, "Brevería ${breveriaId}")
                        poems.add(curPoem)
                    } else {
                        String[] sonnetId = extractSonnetId(line)
                        if (sonnetId != null) {
                            // This is a new sonnet
                            curPoem = new Poem(Poem.PoemType.SONNET, sonnetId[0], curPageId, sonnetId[1])
                            poems.add(curPoem)
                        } else if (curPoem != null) {
                            String[] locationDate = extractLocationDate(line)
                            if (locationDate != null) {
                                // This is the timestamp of the poem
                                // (the end of the poem)
                                curPoem.location = locationDate[0]
                                curPoem.date = locationDate[1]
                                curPoem = null
                            } else if (curPoem != null) {
                                if (!line.isEmpty()
                                        && curPoem.type == Poem.PoemType.BREVERIA
                                        && curPoem.content ==~ /(?s)^.*( *\n){4}$/) {
                                    // older documents may have a poem after a breveria
                                    // without any indication except multiple blank lines
                                    curPoem = new Poem(Poem.PoemType.POEM, null, curPageId, line)
                                    poems.add(curPoem)
                                } else {
                                    // This is content of the poem
                                    curPoem.content += line + "\n"
                                }
                            }
                        } else if (!line.isEmpty()) {
                            curPoem = new Poem(Poem.PoemType.POEM, null, curPageId, line)
                            poems.add(curPoem)
                        }
                    }
                }
            }
        }
        cleanupPoems(poems)
        removeBogusPoems(poems)
        return poems
    }
}
