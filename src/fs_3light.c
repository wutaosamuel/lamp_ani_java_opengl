#version 330 core

in vec3 aPos;
in vec3 aNormal;
in vec2 aTexCoord;

struct PointLight {
  vec3 position;

  float constant;
  float linear;
  float quadratic;

  vec3 ambient;
  vec3 diffuse;
  vec3 specular;
};

struct SpotLight {
  vec3 position;
  vec3 direction;
  float cutOff;
  float outerCutOff;

  float constant;
  float linear;
  float quadratic;

  vec3 ambient;
  vec3 diffuse;
  vec3 specular;
};

#define NR_POINT_LIGHTS 2

struct Material {
  sampler2D diffuse;
  sampler2D specular;
  float shininess;
};

out vec4 fragColor;

uniform vec3 viewPos;
uniform PointLight light1, light2;
uniform SpotLight spotLight;
uniform Material material;
uniform float switchLampState;
uniform float switchWorldState1;
uniform float switchWorldState2;
// function prototypes
vec3 CalcPointLight(PointLight light, vec3 normal, vec3 fragPos, vec3 viewDir, float switch1);
vec3 CalcSpotLight(SpotLight light, vec3 normal, vec3 fragPos, vec3 viewDir);

void main()
{
  vec3 norm = normalize(aNormal);
  vec3 viewDir = normalize(viewPos - aPos);

  vec3 result = CalcSpotLight(spotLight, norm, aPos, viewDir);
  result += CalcPointLight(light1, norm, aPos, viewDir, switchWorldState1);
  result += CalcPointLight(light2, norm, aPos, viewDir, switchWorldState2);

  fragColor = vec4(result, 1.0);
}

vec3 CalcPointLight(PointLight light, vec3 normal, vec3 fragPos, vec3 viewDir, float switch1)
{
  vec3 lightDir = normalize(light.position - fragPos);
  // diffuse shading
  float diff = max(dot(normal, lightDir), 0.0);
  // specular shading
  vec3 reflectDir = reflect(-lightDir, normal);
  float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);
  // attenuation
  float distance = length(light.position - fragPos);
  float attenuation = 1.0 / (light.constant + light.linear * distance + light.quadratic * (distance * distance));
  // combine results
  vec3 ambient = light.ambient * vec3(texture(material.diffuse, aTexCoord));
  vec3 diffuse = light.diffuse * diff * vec3(texture(material.diffuse, aTexCoord));
  vec3 specular = light.specular * spec * vec3(texture(material.specular, aTexCoord));
  ambient *= attenuation;
  diffuse *= attenuation;
  specular *= attenuation;
  return (ambient + diffuse + specular) * switch1;
}

vec3 CalcSpotLight(SpotLight light, vec3 normal, vec3 fragPos, vec3 viewDir)
{
  vec3 lightDir = normalize(light.position - fragPos);
  // diffuse shading
  float diff = max(dot(normal, lightDir), 0.0);
  // specular shading
  vec3 reflectDir = reflect(-lightDir, normal);
  float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);
  // attenuation
  float distance = length(light.position - fragPos);
  float attenuation = 1.0 / (light.constant + light.linear * distance + light.quadratic * (distance * distance));
  // spotlight results
  float theta = dot(lightDir, normalize(-light.direction));
  float epsilon = light.cutOff - light.outerCutOff;
  float intensity = clamp((theta - light.outerCutOff) / epsilon, 0.0, 1.0);
  // combine results
  vec3 ambient = light.ambient * vec3(texture(material.diffuse, aTexCoord));
  vec3 diffuse = light.diffuse * diff * vec3(texture(material.diffuse, aTexCoord));
  vec3 specular = light.specular * spec * vec3(texture(material.specular, aTexCoord));
  ambient *= attenuation * intensity;
  diffuse *= attenuation * intensity;
  specular *= attenuation * intensity;
  return (ambient + diffuse + specular) * switchLampState;
}
