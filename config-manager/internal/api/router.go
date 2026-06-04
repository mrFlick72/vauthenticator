package api

import (
	"net/http"
	"time"

	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
	"github.com/mrFlick72/vauthenticator/config-manager/internal/config"
)

func NewRouter(cfg config.Config) *gin.Engine {
	router := gin.New()
	router.Use(gin.Recovery())
	router.Use(cors.New(cors.Config{
		AllowOrigins: []string{cfg.ManagementUIServerURL},
		AllowMethods: []string{http.MethodGet, http.MethodOptions},
		AllowHeaders: []string{"Accept", "Content-Type", "Origin"},
		MaxAge:       12 * time.Hour,
	}))

	router.GET("/api/config", func(ctx *gin.Context) {
		ctx.JSON(http.StatusOK, cfg.Application)
	})

	return router
}
