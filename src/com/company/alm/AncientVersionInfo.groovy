package com.project.alm


class AncientVersionInfo {

    boolean notSupported
    boolean hasAncient
    boolean isBlue
    boolean isRenamed
    boolean isConsolidated = true

    AncientVersionInfo() {
        notSupported = true
    }

    AncientVersionInfo(boolean notSupported) {
        this.notSupported = notSupported
    }

    String toString() {
        return "\nAncientVersionInfo:\n" +
                "\tnotSupported: ${notSupported}\n"
        "\thasAncient: ${hasAncient}\n"
        "\tisBlue: ${isBlue}\n"
        "\tisRenamed: ${isRenamed}\n"
        "\tisConsolidated: ${isConsolidated}\n"
    }

}
