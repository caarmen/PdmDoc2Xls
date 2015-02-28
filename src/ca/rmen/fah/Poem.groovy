package ca.rmen.fah
/**
 * Created by calvarez on 28/02/15.
 */
class Poem {
    enum PoemType {
        BREVERIA, SONNET, POEM
    }
    PoemType type
    String id
    String pageId
    String title
    String precontent
    String content
    String date
    String location

    Poem() {
        content = ""
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
