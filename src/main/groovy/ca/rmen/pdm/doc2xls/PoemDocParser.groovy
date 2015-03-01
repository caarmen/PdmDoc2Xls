package ca.rmen.pdm.doc2xls

/**
 * Created by calvarez on 28/02/15.
 */
class PoemDocParser {

    private static String extractPageId(String input) {
        // Special bug cases:
        if("4 – Cuarta de diciembre de 2009".equals(input))
            input = "d – Cuarta de diciembre de 2009"

        def pattern = ~/^([a-z]) *[–-] (\p{L}*) de (\p{L}+) de ([0-9]{4})/
        def matcher = pattern.matcher(input)
        if (matcher.matches())
            return matcher.group(4) + "-" + matcher.group(3) + "-" + matcher.group(1)

        // Older documents (early 2001):
        pattern = ~/^Poemas de (\p{L}+) (de )?([0-9]{4})/
        matcher = pattern.matcher(input)
        if (matcher.matches())
            return matcher.group(3) + "-" + matcher.group(1)

        // Even older:
        pattern = ~/^.*\((\p{L}+) (199[89])\)\s*$/
        matcher = pattern.matcher(input)
        if (matcher.matches())
            return matcher.group(2) + "-" + matcher.group(1)

        // Special cases:
        if (input.equals("Haikú"))
            return input
        else if(input.equals("Tankas"))
            return input
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
        def pattern = ~/^([0-9]+) ?[–-] ?(.+)$/
        def matcher = pattern.matcher(input.trim())
        if (matcher.matches())
            return [matcher.group(1), matcher.group(2)]
        return null
    }

    private static String[] extractLocationDate(String input) {
        def pattern = ~/^([\p{L} \(\)]+)[\.,]? *([0-9]+) de *(\p{L}*),? (de )?([0-9]{4})[\.,\/ ]*/
        def matcher = pattern.matcher(input)
        if (matcher.matches())
            return [matcher.group(1), matcher.group(5) + "-" + matcher.group(3) + "-" + matcher.group(2)]

        // Special cases not easily handled with regex:
        if("Los Angeles, Nochevieja de 2003 ".equals(input))
            return ["Los Angeles", "2003-diciembre-31"]

        // Some bugs
        // One bug in 1998 where the year was not included:
        if ("Los Angeles, 21 de diciembre".equals(input))
            return ["Los Angeles", "1998-diciembre-21"]
        // Date formatting bugs:
        if ("Los Angeles, a 2 de diciembre de 2009".equals(input))
            return ["Los Angeles", "2009-diciembre-2"]
        if ("Los Angeles, 15 diciembre de 2010".equals(input))
            return ["Los Angeles", "2010-diciembre-15"]

        return null
    }

    private static void cleanupPoems(List<Poem> poems) {
        for (Poem poem : poems) {
            poem.content = poem.content.trim()
            if (poem.id != null)
                poem.id = poem.id.trim()
            // Special case
            if(poem.type == Poem.PoemType.SONNET) {
                if(poem.id == "704" || poem.id == "705") {
                    poem.location = "Los Angeles"
                    poem.date = "2003-enero-10"
                }
            }
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
                        // This is a new breveria (or in rare cases, haiku)
                        Poem.PoemType poemType;
                        if("Haikú".equals(curPageId))
                            poemType = Poem.PoemType.HAIKU
                        else if("Tankas".equals(curPageId))
                            poemType = Poem.PoemType.TANKA
                        else
                            poemType = Poem.PoemType.BREVERIA

                        curPoem = new Poem(poemType, breveriaId, curPageId, "Brevería ${breveriaId}")
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
