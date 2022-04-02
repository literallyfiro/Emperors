package me.onlyfire.emperors.bot;


public record Emperor(long groupId, String name, String photoId, long takenById, String takenByName, long takenTime) {

    @Override
    public long groupId() {
        return groupId;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String photoId() {
        return photoId;
    }

    @Override
    public long takenById() {
        return takenById;
    }

    @Override
    public String takenByName() {
        return takenByName;
    }

    @Override
    public long takenTime() {
        return takenTime;
    }
}
