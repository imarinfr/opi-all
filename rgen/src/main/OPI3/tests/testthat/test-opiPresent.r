test_that("Sims", {

    for (m in c("SimYes", "SimNo", "SimGaussian", "SimHenson")) {
        a <- do.call(paste0("opiPresent_for_", m), args = list(stim = list(x = 0, y = 0, level = 10)))
        expect_named(a, c("err", "seen", "time"), label = m)
        expect_equal(a$err, NULL, label = m)

        if (m == "SimYes") {
            expect_equal(a$seen, TRUE, label = m)
            expect_equal(a$time, NA, label = m)
        }

        if (m == "SimNo") {
            expect_equal(a$seen, FALSE, label = m)
            expect_equal(a$time, NA, label = m)
        }
    }
})