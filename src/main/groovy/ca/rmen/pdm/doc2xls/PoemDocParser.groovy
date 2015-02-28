package ca.rmen.pdm.doc2xls

/**
 * Created by calvarez on 28/02/15.
 */
class PoemDocParser {

    private static String extractPageId(String input) {
        def pattern = ~/^([a-z]) *[–-] (\p{L}*) de ([a-z]+) de ([0-9]{4})/
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
        def pattern = ~/^([\p{L} ]+)[\.,]? ([0-9]+) de (\p{L}*) (de )?([0-9]{4})[\. ]*/
        def matcher = pattern.matcher(input)
        if (matcher.matches())
            return [matcher.group(1), matcher.group(5) + "-" + matcher.group(3) + "-" + matcher.group(2)]
        return null
    }

    private static boolean isPotentialPoemTitle(String input) {
        if (input.isEmpty()
                || input ==~ /^ *Breverías *$/
                || input ==~ /^ *Imagenes *$/)
            return false;
        return true;
    }

    static Poem[] parse(String textFileName) {
        def file = new File(textFileName)
        def poems = []
        file.withReader { reader ->
            def line
            Poem curPoem
            String curPageId
            int i;
            while ((line = reader.readLine()) != null) {
                i++;
                String pageId = extractPageId(line)
                // This is the beginning of a new page
                if (pageId != null) {
                    curPageId = pageId
                } else if (curPageId != null) {
                    String breveriaId = extractBreveriaId(line)
                    if (breveriaId != null) {
                        // This is a new breveria
                        curPoem = new Poem()
                        curPoem.type = Poem.PoemType.BREVERIA
                        curPoem.id = breveriaId
                        curPoem.title = "Brevería ${breveriaId}"
                        curPoem.pageId = curPageId
                        poems.add(curPoem)
                    } else {
                        String[] sonnetId = extractSonnetId(line)
                        if (sonnetId != null) {
                            // This is a new sonnet
                            curPoem = new Poem()
                            curPoem.type = Poem.PoemType.SONNET
                            curPoem.id = sonnetId[0]
                            curPoem.title = sonnetId[1]
                            curPoem.pageId = curPageId
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
                                // This is content of the poem
                                curPoem.content += line + "\n"
                            }
                        } else if (isPotentialPoemTitle(line)) {
                            curPoem = new Poem()
                            curPoem.type = Poem.PoemType.POEM
                            curPoem.title = line
                            curPoem.pageId = curPageId
                            poems.add(curPoem)
                        }
                    }
                }
            }
        }
        for (Poem poem : poems) {
            poem.content = poem.content.trim()
        }
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
        return poems
    }
}
