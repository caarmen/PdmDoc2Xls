package ca.rmen.pdm.doc2xls
/**
 * Created by calvarez on 28/02/15.
 */
class Poem {
    enum PoemType {
        BREVERIA, SONNET, HAIKU, TANKA, POEM
    }
    PoemType type
    String id
    String pageId
    String title
    String precontent
    String content
    String date
    String location

    Poem(PoemType type, String id, String pageId, String title) {
        this.type = type
        this.id = id
        this.pageId = pageId
        this.title = title
        this.content = ""
    }

    @Override
    public String toString() {
        return "Poem{" +
                "id=" + id +
                ", type=" + type +
                ", pageId=" + pageId +
                ", title='" + title + '\'' +
                ", precontent='" + precontent + '\'' +
                ", content='" + content + '\'' +
                ", location='" + location + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
