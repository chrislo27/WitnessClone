package io.github.chrislo27.witnessclone.puzzle


enum class PuzzleMaterial(
        val startTracing: String,
        val abortTracing: String,
        val finishTracing: String,
        val abortFinishTracing: String,
        val success: String,
        val failure: String,
        val potentialFailure: String,
        val scintStart: String,
        val scintEnd: String
                         ) {
    NORMAL("panel_start_tracing", "panel_abort_tracing", "panel_finish_tracing", "panel_abort_finish_tracing", "panel_success", "panel_failure", "panel_potential_failure", "panel_scint_start", "panel_scint_end"),
    CRT("panel_crt_start_tracing", "panel_crt_abort_tracing", "panel_crt_finish_tracing", "panel_crt_abort_finish_tracing", "panel_crt_success", "panel_crt_failure", "panel_crt_potential_failure", "panel_crt_scint_start", "panel_crt_scint_end"),
    GLASS("panel_glass_start_tracing", "panel_glass_abort_tracing", "panel_glass_finish_tracing", "panel_glass_abort_finish_tracing", "panel_glass_success", "panel_glass_failure", "panel_glass_potential_failure", "panel_glass_scint_start", "panel_glass_scint_end")
}