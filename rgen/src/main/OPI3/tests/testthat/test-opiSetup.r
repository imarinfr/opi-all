test_that("Sims", {
    for (m in c("SimYes", "SimNo", "SimGaussian", "SimHenson"))
          expect_equal(do.call(paste0("opiSetup_for_", m), args = list()), list(err = NULL), label = m)
})