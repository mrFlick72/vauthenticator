package config

import (
	"errors"
	"fmt"
	"os"
	"strings"

	"github.com/spf13/viper"
)

const (
	envConfigFile                  = "CONFIG_MANAGER_ENV_FILE"
	envServerAddress               = "SERVER_ADDRESS"
	envManagementUIServerURL       = "MANAGEMENT_UI_SERVER_URL"
	envIDPBaseURL                  = "IDP_BASE_URL"
	envClientApplicationID         = "CLIENT_APPLICATION_ID"
	envRedirectURI                 = "REDIRECT_URI"
	envAuthenticationCheckInterval = "AUTHENTICATION_CHECK_INTERVAL"
	envAPIBaseURL                  = "API_BASE_URL"
)

const defaultConfigFile = ".env"

type Config struct {
	ServerAddress         string
	ManagementUIServerURL string
	Application           ApplicationConfig
}

type ApplicationConfig struct {
	IDPBaseURL                  string `json:"idpBaseUrl"`
	ClientApplicationID         string `json:"clientApplicationId"`
	RedirectURI                 string `json:"redirectUri"`
	AuthenticationCheckInterval string `json:"authenticationCheckInterval"`
	APIBaseURL                  string `json:"apiBaseUrl"`
}

func Load() (Config, error) {
	configFile, configured := configFilePath()

	v := viper.New()
	v.SetConfigFile(configFile)
	v.SetConfigType("env")
	v.SetDefault(envServerAddress, ":8086")
	v.AutomaticEnv()

	if err := v.ReadInConfig(); err != nil {
		if configured {
			return Config{}, fmt.Errorf("read config file from %s %q: %w", envConfigFile, configFile, err)
		}

		var notFound viper.ConfigFileNotFoundError
		if !errors.As(err, &notFound) && !os.IsNotExist(err) {
			return Config{}, fmt.Errorf("read config file %q: %w", configFile, err)
		}
	}

	return Parse(v)
}

func configFilePath() (path string, configured bool) {
	path = strings.TrimSpace(os.Getenv(envConfigFile))
	if path == "" {
		return defaultConfigFile, false
	}

	return path, true
}

func Parse(v *viper.Viper) (Config, error) {
	cfg := Config{
		ServerAddress:         strings.TrimSpace(v.GetString(envServerAddress)),
		ManagementUIServerURL: strings.TrimSpace(v.GetString(envManagementUIServerURL)),
		Application: ApplicationConfig{
			IDPBaseURL:                  strings.TrimSpace(v.GetString(envIDPBaseURL)),
			ClientApplicationID:         strings.TrimSpace(v.GetString(envClientApplicationID)),
			RedirectURI:                 strings.TrimSpace(v.GetString(envRedirectURI)),
			AuthenticationCheckInterval: strings.TrimSpace(v.GetString(envAuthenticationCheckInterval)),
			APIBaseURL:                  strings.TrimSpace(v.GetString(envAPIBaseURL)),
		},
	}

	if cfg.ServerAddress == "" {
		cfg.ServerAddress = ":8086"
	}

	if err := cfg.Validate(); err != nil {
		return Config{}, err
	}

	return cfg, nil
}

func (c Config) Validate() error {
	missing := make([]string, 0)

	if c.ManagementUIServerURL == "" {
		missing = append(missing, envManagementUIServerURL)
	}
	if c.Application.IDPBaseURL == "" {
		missing = append(missing, envIDPBaseURL)
	}
	if c.Application.ClientApplicationID == "" {
		missing = append(missing, envClientApplicationID)
	}
	if c.Application.RedirectURI == "" {
		missing = append(missing, envRedirectURI)
	}
	if c.Application.AuthenticationCheckInterval == "" {
		missing = append(missing, envAuthenticationCheckInterval)
	}
	if c.Application.APIBaseURL == "" {
		missing = append(missing, envAPIBaseURL)
	}

	if len(missing) > 0 {
		return fmt.Errorf("missing required configuration: %s", strings.Join(missing, ", "))
	}

	return nil
}
