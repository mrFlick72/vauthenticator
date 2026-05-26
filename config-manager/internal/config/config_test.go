package config

import (
	"os"
	"path/filepath"
	"strings"
	"testing"

	"github.com/spf13/viper"
)

func TestParseMapsEnvironmentConfiguration(t *testing.T) {
	v := viper.New()
	v.Set(envServerAddress, ":8090")
	v.Set(envManagementUIServerURL, " http://local.management.vauthenticator.com:8085 ")
	v.Set(envIDPBaseURL, " http://local.api.vauthenticator.com:9090 ")
	v.Set(envClientApplicationID, " vauthenticator-management-ui ")
	v.Set(envRedirectURI, " http://local.management.vauthenticator.com:8085/callback ")
	v.Set(envAuthenticationCheckInterval, " 15000 ")
	v.Set(envAPIBaseURL, " http://local.api.vauthenticator.com:9090/api ")

	cfg, err := Parse(v)
	if err != nil {
		t.Fatalf("Parse() error = %v", err)
	}

	if cfg.ServerAddress != ":8090" {
		t.Fatalf("ServerAddress = %q; want %q", cfg.ServerAddress, ":8090")
	}
	if cfg.ManagementUIServerURL != "http://local.management.vauthenticator.com:8085" {
		t.Fatalf("ManagementUIServerURL = %q", cfg.ManagementUIServerURL)
	}

	want := ApplicationConfig{
		IDPBaseURL:                  "http://local.api.vauthenticator.com:9090",
		ClientApplicationID:         "vauthenticator-management-ui",
		RedirectURI:                 "http://local.management.vauthenticator.com:8085/callback",
		AuthenticationCheckInterval: "15000",
		APIBaseURL:                  "http://local.api.vauthenticator.com:9090/api",
	}
	if cfg.Application != want {
		t.Fatalf("Application = %+v; want %+v", cfg.Application, want)
	}
}

func TestParseDefaultsServerAddress(t *testing.T) {
	v := viper.New()
	v.Set(envManagementUIServerURL, "http://local.management.vauthenticator.com:8085")
	v.Set(envIDPBaseURL, "http://local.api.vauthenticator.com:9090")
	v.Set(envClientApplicationID, "vauthenticator-management-ui")
	v.Set(envRedirectURI, "http://local.management.vauthenticator.com:8085/callback")
	v.Set(envAuthenticationCheckInterval, "15000")
	v.Set(envAPIBaseURL, "http://local.api.vauthenticator.com:9090/api")

	cfg, err := Parse(v)
	if err != nil {
		t.Fatalf("Parse() error = %v", err)
	}

	if cfg.ServerAddress != ":8086" {
		t.Fatalf("ServerAddress = %q; want %q", cfg.ServerAddress, ":8086")
	}
}

func TestParseRejectsMissingRequiredConfiguration(t *testing.T) {
	_, err := Parse(viper.New())
	if err == nil {
		t.Fatal("Parse() error = nil; want missing configuration error")
	}

	msg := err.Error()
	for _, name := range []string{
		envManagementUIServerURL,
		envIDPBaseURL,
		envClientApplicationID,
		envRedirectURI,
		envAuthenticationCheckInterval,
		envAPIBaseURL,
	} {
		if !strings.Contains(msg, name) {
			t.Fatalf("error %q does not include missing key %s", msg, name)
		}
	}
}

func TestLoadReadsEnvironmentVariablesWithoutEnvFile(t *testing.T) {
	t.Chdir(t.TempDir())
	setRequiredEnv(t)

	cfg, err := Load()
	if err != nil {
		t.Fatalf("Load() error = %v", err)
	}

	if cfg.Application.ClientApplicationID != "vauthenticator-management-ui" {
		t.Fatalf("ClientApplicationID = %q", cfg.Application.ClientApplicationID)
	}
}

func TestLoadReadsEnvFile(t *testing.T) {
	dir := t.TempDir()
	t.Chdir(dir)

	if err := os.WriteFile(filepath.Join(dir, ".env"), []byte(testEnvFile()), 0o600); err != nil {
		t.Fatalf("write .env: %v", err)
	}

	cfg, err := Load()
	if err != nil {
		t.Fatalf("Load() error = %v", err)
	}

	if cfg.ManagementUIServerURL != "http://local.management.vauthenticator.com:8085" {
		t.Fatalf("ManagementUIServerURL = %q", cfg.ManagementUIServerURL)
	}
}

func TestLoadReadsConfiguredEnvFile(t *testing.T) {
	dir := t.TempDir()
	t.Chdir(dir)

	configPath := filepath.Join(dir, "config", "config-manager.env")
	if err := os.MkdirAll(filepath.Dir(configPath), 0o700); err != nil {
		t.Fatalf("create config directory: %v", err)
	}
	if err := os.WriteFile(configPath, []byte(testEnvFile()), 0o600); err != nil {
		t.Fatalf("write configured env file: %v", err)
	}
	t.Setenv(envConfigFile, configPath)

	cfg, err := Load()
	if err != nil {
		t.Fatalf("Load() error = %v", err)
	}

	if cfg.Application.APIBaseURL != "http://local.api.vauthenticator.com:9090/api" {
		t.Fatalf("APIBaseURL = %q", cfg.Application.APIBaseURL)
	}
}

func TestLoadFailsWhenConfiguredEnvFileIsMissing(t *testing.T) {
	dir := t.TempDir()
	t.Chdir(dir)
	t.Setenv(envConfigFile, filepath.Join(dir, "missing.env"))
	setRequiredEnv(t)

	_, err := Load()
	if err == nil {
		t.Fatal("Load() error = nil; want configured file read error")
	}

	if !strings.Contains(err.Error(), envConfigFile) && !strings.Contains(err.Error(), "missing.env") {
		t.Fatalf("error %q does not mention configured file", err)
	}
}

func setRequiredEnv(t *testing.T) {
	t.Helper()

	t.Setenv(envManagementUIServerURL, "http://local.management.vauthenticator.com:8085")
	t.Setenv(envIDPBaseURL, "http://local.api.vauthenticator.com:9090")
	t.Setenv(envClientApplicationID, "vauthenticator-management-ui")
	t.Setenv(envRedirectURI, "http://local.management.vauthenticator.com:8085/callback")
	t.Setenv(envAuthenticationCheckInterval, "15000")
	t.Setenv(envAPIBaseURL, "http://local.api.vauthenticator.com:9090/api")
}

func testEnvFile() string {
	return strings.Join([]string{
		`MANAGEMENT_UI_SERVER_URL="http://local.management.vauthenticator.com:8085"`,
		`IDP_BASE_URL="http://local.api.vauthenticator.com:9090"`,
		`CLIENT_APPLICATION_ID="vauthenticator-management-ui"`,
		`REDIRECT_URI="http://local.management.vauthenticator.com:8085/callback"`,
		`AUTHENTICATION_CHECK_INTERVAL="15000"`,
		`API_BASE_URL="http://local.api.vauthenticator.com:9090/api"`,
		"",
	}, "\n")
}
