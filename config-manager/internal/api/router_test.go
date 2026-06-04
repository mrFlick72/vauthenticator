package api

import (
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/gin-gonic/gin"
	"github.com/mrFlick72/vauthenticator/config-manager/internal/config"
)

func TestConfigEndpointReturnsApplicationConfig(t *testing.T) {
	gin.SetMode(gin.TestMode)

	cfg := testConfig()
	router := NewRouter(cfg)

	req := httptest.NewRequest(http.MethodGet, "/api/config", nil)
	req.Header.Set("Origin", cfg.ManagementUIServerURL)

	rec := httptest.NewRecorder()
	router.ServeHTTP(rec, req)

	if rec.Code != http.StatusOK {
		t.Fatalf("status = %d; want %d", rec.Code, http.StatusOK)
	}

	if got := rec.Header().Get("Access-Control-Allow-Origin"); got != cfg.ManagementUIServerURL {
		t.Fatalf("Access-Control-Allow-Origin = %q; want %q", got, cfg.ManagementUIServerURL)
	}

	var got config.ApplicationConfig
	if err := json.Unmarshal(rec.Body.Bytes(), &got); err != nil {
		t.Fatalf("decode response: %v", err)
	}

	if got != cfg.Application {
		t.Fatalf("response = %+v; want %+v", got, cfg.Application)
	}
}

func TestConfigEndpointAllowsCorsPreflight(t *testing.T) {
	gin.SetMode(gin.TestMode)

	cfg := testConfig()
	router := NewRouter(cfg)

	req := httptest.NewRequest(http.MethodOptions, "/api/config", nil)
	req.Header.Set("Origin", cfg.ManagementUIServerURL)
	req.Header.Set("Access-Control-Request-Method", http.MethodGet)

	rec := httptest.NewRecorder()
	router.ServeHTTP(rec, req)

	if rec.Code != http.StatusNoContent {
		t.Fatalf("status = %d; want %d", rec.Code, http.StatusNoContent)
	}

	if got := rec.Header().Get("Access-Control-Allow-Origin"); got != cfg.ManagementUIServerURL {
		t.Fatalf("Access-Control-Allow-Origin = %q; want %q", got, cfg.ManagementUIServerURL)
	}
}

func testConfig() config.Config {
	return config.Config{
		ServerAddress:         ":8086",
		ManagementUIServerURL: "http://local.management.vauthenticator.com:8085",
		Application: config.ApplicationConfig{
			IDPBaseURL:                  "http://local.api.vauthenticator.com:9090",
			ClientApplicationID:         "vauthenticator-management-ui",
			RedirectURI:                 "http://local.management.vauthenticator.com:8085/callback",
			AuthenticationCheckInterval: "15000",
			APIBaseURL:                  "http://local.api.vauthenticator.com:9090/api",
		},
	}
}
